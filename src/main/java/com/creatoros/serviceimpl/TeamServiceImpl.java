package com.creatoros.serviceimpl;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatoros.dao.CreatorDao;
import com.creatoros.dao.TeamInvitationDao;
import com.creatoros.dao.TeamMemberDao;
import com.creatoros.dto.team.CreateTeamInvitationRequest;
import com.creatoros.dto.team.TeamInvitationDto;
import com.creatoros.dto.team.TeamMemberDto;
import com.creatoros.dto.team.UpdateTeamMemberRequest;
import com.creatoros.entity.Creator;
import com.creatoros.entity.TeamInvitation;
import com.creatoros.entity.TeamMember;
import com.creatoros.enums.PermissionKey;
import com.creatoros.enums.Role;
import com.creatoros.exception.BadRequestException;
import com.creatoros.exception.ResourceNotFoundException;
import com.creatoros.security.SecurityUtils;
import com.creatoros.security.TeamAccessResolver;
import com.creatoros.service.TeamService;
import com.creatoros.util.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final CreatorDao        creatorDao;
    private final TeamMemberDao     teamMemberDao;
    private final TeamInvitationDao teamInvitationDao;
    private final TeamMapper        teamMapper;
    private final EmailService      emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberDto> listMembers(Long creatorId) {
        requireOwnerOrAdmin();
        return teamMemberDao.findByCreatorIdOrderByCreatedAtDesc(creatorId).stream().map(teamMapper::toMemberDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamInvitationDto> listInvitations(Long creatorId) {
        requireOwnerOrAdmin();
        return teamInvitationDao.findPendingByCreatorIdOrderByCreatedAtDesc(creatorId).stream().map(teamMapper::toInvitationDto).toList();
    }

    @Override
    @Transactional
    public TeamInvitationDto inviteMember(Long creatorId, CreateTeamInvitationRequest request) {
        requireOwnerOrAdmin();
        Role role = requireAssignableTeamRole(request.role());
        String email = normalizeEmail(request.email());
        Creator owner = requireCreator(creatorId);

        if (email.equalsIgnoreCase(normalizeEmail(owner.getEmail()))) {
            throw new BadRequestException("You cannot invite yourself to your own team.", "SELF_INVITE_NOT_ALLOWED");
        }

        creatorDao.findByEmailIgnoreCase(email).ifPresent(existingCreator -> {
            if (teamMemberDao.findByCreatorIdAndMemberCreatorId(creatorId, existingCreator.getId()).isPresent()) {
                throw new BadRequestException("This member is already in your team.", "MEMBER_EXISTS");
            }
        });

        TeamInvitation invitation = teamInvitationDao.findByCreatorIdAndEmailIgnoreCaseAndRevokedFalse(creatorId, email).map(existing -> {
            existing.setRole(role);
            existing.setExpiresOn(LocalDate.now().plusDays(14));
            existing.setInviteToken(generateToken());
            existing.setRevoked(false);
            return existing;
        }).orElseGet(() -> TeamInvitation.builder().creatorId(creatorId).email(email).role(role).inviteToken(generateToken())
                .expiresOn(LocalDate.now().plusDays(14)).build());

        teamInvitationDao.save(invitation);
        try {
            sendInvitationEmail(owner, invitation);
        } catch (IllegalStateException ex) {
            log.warn("Failed to send team invitation email to {}: {}", invitation.getEmail(), ex.getMessage());
        }
        return teamMapper.toInvitationDto(invitation);
    }

    @Override
    @Transactional
    public TeamMemberDto updateMember(Long creatorId, Long memberId, UpdateTeamMemberRequest request) {
        requireOwnerOrAdmin();
        Role role = requireAssignableTeamRole(request.role());
        TeamMember member = teamMemberDao.findByIdAndCreatorId(memberId, creatorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Team member", memberId));

        if (request.active()) {
            assertNoConflictingActiveMembership(member.getMemberCreatorId(), creatorId);
        }

        member.setRole(role);
        member.setActive(request.active());
        member.setPermissions(normalizePermissions(request.permissions(), role));
        teamMemberDao.save(member);
        return teamMapper.toMemberDto(member);
    }

    @Override
    @Transactional
    public void revokeInvitation(Long creatorId, Long invitationId) {
        requireOwnerOrAdmin();
        TeamInvitation invitation = teamInvitationDao.findByIdAndCreatorId(invitationId, creatorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Team invitation", invitationId));
        invitation.setRevoked(true);
        teamInvitationDao.save(invitation);
    }

    @Override
    @Transactional
    public TeamMemberDto acceptInvitation(String inviteToken, Long acceptingCreatorId) {
        TeamInvitation invitation = teamInvitationDao.findByInviteToken(inviteToken)
                .orElseThrow(() -> ResourceNotFoundException.of("Team invitation", inviteToken));

        if (invitation.isRevoked() || invitation.getAcceptedAt() != null || invitation.getExpiresOn().isBefore(LocalDate.now())) {
            throw new BadRequestException("This invitation is no longer valid.", "INVITATION_EXPIRED");
        }

        Creator acceptingCreator = creatorDao.findById(acceptingCreatorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Creator", acceptingCreatorId));

        if (!acceptingCreator.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new BadRequestException("This invitation was sent to a different email address.", "INVITATION_EMAIL_MISMATCH");
        }

        assertNoConflictingActiveMembership(acceptingCreatorId, invitation.getCreatorId());

        TeamMember member = teamMemberDao.findByCreatorIdAndMemberCreatorId(invitation.getCreatorId(), acceptingCreatorId)
                .orElseGet(TeamMember::new);
        member.setCreatorId(invitation.getCreatorId());
        member.setMemberCreatorId(acceptingCreatorId);
        member.setEmail(acceptingCreator.getEmail());
        member.setName(acceptingCreator.getName());
        member.setRole(invitation.getRole());
        member.setActive(true);
        member.setPermissions(TeamAccessResolver.defaultPermissionsForTeamRole(invitation.getRole()));
        member.setInvitedByCreatorId(invitation.getCreatorId());
        teamMemberDao.save(member);

        invitation.setAcceptedAt(new Timestamp(System.currentTimeMillis()));
        invitation.setAcceptedByCreatorId(acceptingCreatorId);
        teamInvitationDao.save(invitation);

        return teamMapper.toMemberDto(member);
    }

    private void requireOwnerOrAdmin() {
        boolean platformAdmin = SecurityUtils.currentPrincipal().map(principal -> principal.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(Role.ADMIN.asAuthority()))).orElse(false);
        if (platformAdmin || SecurityUtils.isTenantOwner()) {
            return;
        }
        throw new BadRequestException("Only the workspace owner can manage team permissions.", "OWNER_REQUIRED");
    }

    private Role requireAssignableTeamRole(Role role) {
        if (!role.isTeamAssignable()) {
            throw new BadRequestException("Role must be one of Manager, Editor, or Accountant.", "TEAM_ROLE_INVALID");
        }
        return role;
    }

    private void assertNoConflictingActiveMembership(Long memberCreatorId, Long targetOwnerCreatorId) {
        teamMemberDao.findByMemberCreatorIdAndActiveTrue(memberCreatorId).filter(existing -> !existing.getCreatorId().equals(targetOwnerCreatorId))
                .ifPresent(existing -> {
                    throw new BadRequestException(
                            "This person is already an active member of another team. They must leave that team before joining a new one.",
                            "ALREADY_ON_ANOTHER_TEAM");
                });
    }

    private Creator requireCreator(Long creatorId) {
        return creatorDao.findById(creatorId).orElseThrow(() -> ResourceNotFoundException.of("Creator", creatorId));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private Set<PermissionKey> normalizePermissions(Set<PermissionKey> permissions, Role role) {
        if (permissions == null || permissions.isEmpty()) {
            return TeamAccessResolver.defaultPermissionsForTeamRole(role);
        }
        return EnumSet.copyOf(permissions);
    }

    private String generateToken() {
        byte[] buffer = new byte[24];
        RANDOM.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }

    private void sendInvitationEmail(Creator owner, TeamInvitation invitation) {
        String invitationUrl = frontendUrl.replaceAll("/+$", "") + "/team/invitations/accept?token=" + invitation.getInviteToken();
        String ownerName = owner.getName() == null || owner.getName().isBlank() ? owner.getEmail() : owner.getName();
        String subject = ownerName + " invited you to join their CreatorOS team";
        String body = "Hi,\n\n" + ownerName + " has invited you to join their CreatorOS team as a " + invitation.getRole().name() + ".\n\n"
                + "Accept the invitation here:\n" + invitationUrl + "\n\n" + "This invitation expires on " + invitation.getExpiresOn() + ".\n\n"
                + "If you were not expecting this invitation, you can ignore this email.\n\n" + "— CreatorOS";
        emailService.sendEmail(invitation.getEmail(), subject, body);
    }

}
