package com.creatoros.serviceimpl;

import java.util.LinkedHashSet;

import org.springframework.stereotype.Component;

import com.creatoros.dto.team.TeamInvitationDto;
import com.creatoros.dto.team.TeamMemberDto;
import com.creatoros.entity.TeamInvitation;
import com.creatoros.entity.TeamMember;

@Component
public class TeamMapper {

    public TeamMemberDto toMemberDto(TeamMember member) {
        return new TeamMemberDto(idOf(member.getId()), idOf(member.getCreatorId()), idOf(member.getMemberCreatorId()), member.getEmail(),
                member.getName(), member.getRole(), member.isActive(),
                member.getPermissions() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(member.getPermissions()), member.getCreatedAt(),
                member.getUpdatedAt());
    }

    public TeamInvitationDto toInvitationDto(TeamInvitation invitation) {
        return new TeamInvitationDto(idOf(invitation.getId()), idOf(invitation.getCreatorId()), invitation.getEmail(),
                invitation.getInviteToken(), invitation.getRole(), invitation.getExpiresOn(), invitation.getAcceptedAt(),
                idOf(invitation.getAcceptedByCreatorId()), invitation.isRevoked(), invitation.getCreatedAt(), invitation.getUpdatedAt());
    }

    private static String idOf(Long id) {
        return id == null ? null : String.valueOf(id);
    }
}
