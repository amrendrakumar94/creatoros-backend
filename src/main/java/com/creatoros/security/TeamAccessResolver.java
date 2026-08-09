package com.creatoros.security;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.creatoros.dao.TeamMemberDao;
import com.creatoros.entity.TeamMember;
import com.creatoros.enums.PermissionKey;
import com.creatoros.enums.Role;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TeamAccessResolver {

    private final TeamMemberDao teamMemberDao;

    public Optional<TeamMember> activeMembership(Long creatorId) {
        return teamMemberDao.findByMemberCreatorIdAndActiveTrue(creatorId);
    }

    public Long resolveTenantId(Long creatorId) {
        return activeMembership(creatorId).map(TeamMember::getCreatorId).orElse(creatorId);
    }

    public Set<PermissionKey> resolvePermissions(Long creatorId) {
        Optional<TeamMember> membership = activeMembership(creatorId);
        if (membership.isEmpty()) {
            return Set.of(PermissionKey.values());
        }
        Set<PermissionKey> permissions = membership.get().getPermissions();
        return permissions == null ? Set.of() : new LinkedHashSet<>(permissions);
    }

    public Role resolveWorkspaceRole(Long creatorId) {
        return activeMembership(creatorId).map(TeamMember::getRole).orElse(Role.CREATOR);
    }

    public static Set<PermissionKey> defaultPermissionsForTeamRole(Role role) {
        return switch (role) {
            case MANAGER -> EnumSet.of(PermissionKey.MANAGE_BRANDS, PermissionKey.MANAGE_DEALS, PermissionKey.MANAGE_CAMPAIGNS,
                    PermissionKey.VIEW_DASHBOARD);
            case EDITOR -> EnumSet.of(PermissionKey.MANAGE_CONTENT, PermissionKey.MANAGE_CAMPAIGNS, PermissionKey.MANAGE_DELIVERABLES,
                    PermissionKey.VIEW_DASHBOARD);
            case ACCOUNTANT -> EnumSet.of(PermissionKey.MANAGE_FINANCES, PermissionKey.MANAGE_INVOICES, PermissionKey.MANAGE_PAYMENTS,
                    PermissionKey.MANAGE_EXPENSES, PermissionKey.VIEW_DASHBOARD);
            case CREATOR, ADMIN -> throw new IllegalStateException("Role " + role + " has no default team permission set");
        };
    }
}
