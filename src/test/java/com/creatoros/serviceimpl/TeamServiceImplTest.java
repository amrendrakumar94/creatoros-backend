package com.creatoros.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

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
import com.creatoros.enums.CreatorStatus;
import com.creatoros.enums.Role;
import com.creatoros.exception.BadRequestException;
import com.creatoros.security.CreatorPrincipal;
import com.creatoros.util.EmailService;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    private static final Long OWNER_A = 1L;
    private static final Long OWNER_B = 5L;
    private static final Long MEMBER  = 2L;

    @Mock
    private CreatorDao creatorDao;

    @Mock
    private TeamMemberDao teamMemberDao;

    @Mock
    private TeamInvitationDao teamInvitationDao;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TeamServiceImpl service;

    @BeforeEach
    void setFrontendUrl() {
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:3000");
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("blocks a non-owner, non-admin caller from listing the team")
    void nonOwnerNonAdminBlockedFromManagingTeam() {
        authenticateAsMember(MEMBER, OWNER_A);

        assertThatThrownBy(() -> service.listMembers(OWNER_A)).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("OWNER_REQUIRED"));
    }

    @Test
    @DisplayName("allows the workspace owner to list the team")
    void ownerAllowedToListMembers() {
        authenticateAsOwner(OWNER_A);
        when(teamMemberDao.findByCreatorIdOrderByCreatedAtDesc(OWNER_A)).thenReturn(List.of());

        assertThatCode(() -> service.listMembers(OWNER_A)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("allows a platform admin to manage a team even when they don't own the workspace")
    void platformAdminAllowedEvenWhenNotOwner() {
        authenticateAs(MEMBER, OWNER_A, Role.ADMIN);
        when(teamMemberDao.findByCreatorIdOrderByCreatedAtDesc(OWNER_A)).thenReturn(List.of());

        assertThatCode(() -> service.listMembers(OWNER_A)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("rejects inviting someone with a platform-level role")
    void inviteMemberRejectsPlatformRole() {
        authenticateAsOwner(OWNER_A);

        assertThatThrownBy(() -> service.inviteMember(OWNER_A, new CreateTeamInvitationRequest("x@example.com", Role.ADMIN)))
                .isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("TEAM_ROLE_INVALID"));
    }

    @Test
    @DisplayName("rejects an owner inviting their own email")
    void inviteMemberRejectsSelfInvite() {
        authenticateAsOwner(OWNER_A);
        Creator owner = creator(OWNER_A, "owner@example.com");
        when(creatorDao.findById(OWNER_A)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> service.inviteMember(OWNER_A, new CreateTeamInvitationRequest("Owner@Example.com", Role.MANAGER)))
                .isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("SELF_INVITE_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("still creates the invitation when the invite email fails to send")
    void inviteMemberSurvivesEmailFailure() {
        authenticateAsOwner(OWNER_A);
        Creator owner = creator(OWNER_A, "owner@example.com");
        String inviteeEmail = "invitee@example.com";
        when(creatorDao.findById(OWNER_A)).thenReturn(Optional.of(owner));
        when(creatorDao.findByEmailIgnoreCase(inviteeEmail)).thenReturn(Optional.empty());
        when(teamInvitationDao.findByCreatorIdAndEmailIgnoreCaseAndRevokedFalse(OWNER_A, inviteeEmail)).thenReturn(Optional.empty());
        when(teamMapper.toInvitationDto(any())).thenReturn(
                new TeamInvitationDto("1", "1", inviteeEmail, "token", Role.MANAGER, LocalDate.now().plusDays(14), null, null, false, null, null));
        org.mockito.Mockito.doThrow(new IllegalStateException("smtp down")).when(emailService).sendEmail(any(), any(), any());

        assertThatCode(() -> service.inviteMember(OWNER_A, new CreateTeamInvitationRequest(inviteeEmail, Role.MANAGER)))
                .doesNotThrowAnyException();
        verify(teamInvitationDao).save(any(TeamInvitation.class));
    }

    @Test
    @DisplayName("rejects accepting an invitation while already active on a different team")
    void acceptInvitationRejectsConflictingMembership() {
        String token = "some-token";
        String email = "member@example.com";
        TeamInvitation invitation = TeamInvitation.builder().creatorId(OWNER_B).email(email).role(Role.MANAGER).inviteToken(token)
                .expiresOn(LocalDate.now().plusDays(1)).revoked(false).build();
        Creator acceptingCreator = creator(MEMBER, email);
        TeamMember existingMembership = TeamMember.builder().creatorId(OWNER_A).memberCreatorId(MEMBER).role(Role.EDITOR).active(true).build();

        when(teamInvitationDao.findByInviteToken(token)).thenReturn(Optional.of(invitation));
        when(creatorDao.findById(MEMBER)).thenReturn(Optional.of(acceptingCreator));
        when(teamMemberDao.findByMemberCreatorIdAndActiveTrue(MEMBER)).thenReturn(Optional.of(existingMembership));

        assertThatThrownBy(() -> service.acceptInvitation(token, MEMBER)).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("ALREADY_ON_ANOTHER_TEAM"));
    }

    @Test
    @DisplayName("rejects reactivating a member who has since joined a different team")
    void updateMemberRejectsReactivatingConflictingMembership() {
        authenticateAsOwner(OWNER_A);
        Long memberId = 99L;
        TeamMember member = TeamMember.builder().creatorId(OWNER_A).memberCreatorId(MEMBER).role(Role.EDITOR).active(false).build();
        TeamMember conflictingMembership = TeamMember.builder().creatorId(OWNER_B).memberCreatorId(MEMBER).role(Role.MANAGER).active(true).build();

        when(teamMemberDao.findByIdAndCreatorId(memberId, OWNER_A)).thenReturn(Optional.of(member));
        when(teamMemberDao.findByMemberCreatorIdAndActiveTrue(MEMBER)).thenReturn(Optional.of(conflictingMembership));

        assertThatThrownBy(() -> service.updateMember(OWNER_A, memberId, new UpdateTeamMemberRequest(Role.EDITOR, null, true)))
                .isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("ALREADY_ON_ANOTHER_TEAM"));
    }

    private void authenticateAsOwner(Long creatorId) {
        authenticateAs(creatorId, creatorId, Role.CREATOR);
    }

    private void authenticateAsMember(Long memberCreatorId, Long tenantId) {
        authenticateAs(memberCreatorId, tenantId, Role.CREATOR);
    }

    private void authenticateAs(Long creatorId, Long tenantId, Role platformRole) {
        Creator creator = creator(creatorId, "user" + creatorId + "@example.com");
        creator.setRole(platformRole);
        CreatorPrincipal principal = new CreatorPrincipal(creator, tenantId, java.util.Set.of());
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static Creator creator(Long id, String email) {
        return Creator.builder().id(id).email(email).passwordHash("hash").status(CreatorStatus.ACTIVE).role(Role.CREATOR).name("Test")
                .handle("test-" + id).build();
    }
}
