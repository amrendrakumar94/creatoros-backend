package com.creatoros.service;

import java.util.List;

import com.creatoros.dto.team.CreateTeamInvitationRequest;
import com.creatoros.dto.team.TeamInvitationDto;
import com.creatoros.dto.team.TeamMemberDto;
import com.creatoros.dto.team.UpdateTeamMemberRequest;

public interface TeamService {

    List<TeamMemberDto> listMembers(Long creatorId);

    List<TeamInvitationDto> listInvitations(Long creatorId);

    TeamInvitationDto inviteMember(Long creatorId, CreateTeamInvitationRequest request);

    TeamMemberDto updateMember(Long creatorId, Long memberId, UpdateTeamMemberRequest request);

    void revokeInvitation(Long creatorId, Long invitationId);

    TeamMemberDto acceptInvitation(String inviteToken, Long acceptingCreatorId);
}
