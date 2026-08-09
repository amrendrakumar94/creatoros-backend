package com.creatoros.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creatoros.dto.team.CreateTeamInvitationRequest;
import com.creatoros.dto.team.TeamInvitationDto;
import com.creatoros.dto.team.TeamMemberDto;
import com.creatoros.dto.team.UpdateTeamMemberRequest;
import com.creatoros.security.SecurityUtils;
import com.creatoros.service.TeamService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/members")
    public ResponseEntity<List<TeamMemberDto>> members() {
        return ResponseEntity.ok(teamService.listMembers(SecurityUtils.currentTenantId()));
    }

    @GetMapping("/invitations")
    public ResponseEntity<List<TeamInvitationDto>> invitations() {
        return ResponseEntity.ok(teamService.listInvitations(SecurityUtils.currentTenantId()));
    }

    @PostMapping("/invitations")
    public ResponseEntity<TeamInvitationDto> invite(@Valid @RequestBody CreateTeamInvitationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.inviteMember(SecurityUtils.currentTenantId(), request));
    }

    @PostMapping("/invitations/{inviteToken}/accept")
    public ResponseEntity<TeamMemberDto> acceptInvitation(@PathVariable String inviteToken) {
        return ResponseEntity.ok(teamService.acceptInvitation(inviteToken, SecurityUtils.currentCreatorId()));
    }

    @PutMapping("/members/{memberId}")
    public ResponseEntity<TeamMemberDto> updateMember(@PathVariable Long memberId, @Valid @RequestBody UpdateTeamMemberRequest request) {
        return ResponseEntity.ok(teamService.updateMember(SecurityUtils.currentTenantId(), memberId, request));
    }

    @DeleteMapping("/invitations/{invitationId}")
    public ResponseEntity<Void> revokeInvitation(@PathVariable Long invitationId) {
        teamService.revokeInvitation(SecurityUtils.currentTenantId(), invitationId);
        return ResponseEntity.noContent().build();
    }
}
