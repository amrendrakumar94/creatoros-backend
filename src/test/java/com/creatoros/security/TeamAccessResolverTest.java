package com.creatoros.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creatoros.dao.TeamMemberDao;
import com.creatoros.entity.TeamMember;
import com.creatoros.enums.PermissionKey;
import com.creatoros.enums.Role;

@ExtendWith(MockitoExtension.class)
class TeamAccessResolverTest {

    private static final Long OWNER  = 1L;
    private static final Long MEMBER = 2L;

    @Mock
    private TeamMemberDao teamMemberDao;

    @InjectMocks
    private TeamAccessResolver resolver;

    @Test
    @DisplayName("resolves to the creator's own id when there is no active membership")
    void resolveTenantIdFallsBackToOwnIdWithoutMembership() {
        when(teamMemberDao.findByMemberCreatorIdAndActiveTrue(MEMBER)).thenReturn(Optional.empty());

        assertThat(resolver.resolveTenantId(MEMBER)).isEqualTo(MEMBER);
    }

    @Test
    @DisplayName("resolves to the owner's id when the caller has an active membership")
    void resolveTenantIdFollowsActiveMembership() {
        TeamMember membership = TeamMember.builder().creatorId(OWNER).memberCreatorId(MEMBER).role(Role.MANAGER).active(true).build();
        when(teamMemberDao.findByMemberCreatorIdAndActiveTrue(MEMBER)).thenReturn(Optional.of(membership));

        assertThat(resolver.resolveTenantId(MEMBER)).isEqualTo(OWNER);
    }

    @Test
    @DisplayName("a true owner (no membership row) is unrestricted")
    void resolvePermissionsGrantsEverythingWithoutMembership() {
        when(teamMemberDao.findByMemberCreatorIdAndActiveTrue(MEMBER)).thenReturn(Optional.empty());

        assertThat(resolver.resolvePermissions(MEMBER)).containsExactlyInAnyOrder(PermissionKey.values());
    }

    @Test
    @DisplayName("an active team member is restricted to their stored permission set")
    void resolvePermissionsReadsFromMembership() {
        Set<PermissionKey> granted = new LinkedHashSet<>(Set.of(PermissionKey.VIEW_DASHBOARD, PermissionKey.MANAGE_DEALS));
        TeamMember membership = TeamMember.builder().creatorId(OWNER).memberCreatorId(MEMBER).role(Role.MANAGER).active(true)
                .permissions(granted).build();
        when(teamMemberDao.findByMemberCreatorIdAndActiveTrue(MEMBER)).thenReturn(Optional.of(membership));

        assertThat(resolver.resolvePermissions(MEMBER)).containsExactlyInAnyOrderElementsOf(granted);
    }

    @Test
    @DisplayName("workspace role defaults to CREATOR without an active membership")
    void resolveWorkspaceRoleDefaultsToCreator() {
        when(teamMemberDao.findByMemberCreatorIdAndActiveTrue(MEMBER)).thenReturn(Optional.empty());

        assertThat(resolver.resolveWorkspaceRole(MEMBER)).isEqualTo(Role.CREATOR);
    }

    @Test
    @DisplayName("workspace role reflects the active membership's role")
    void resolveWorkspaceRoleFollowsActiveMembership() {
        TeamMember membership = TeamMember.builder().creatorId(OWNER).memberCreatorId(MEMBER).role(Role.EDITOR).active(true).build();
        when(teamMemberDao.findByMemberCreatorIdAndActiveTrue(MEMBER)).thenReturn(Optional.of(membership));

        assertThat(resolver.resolveWorkspaceRole(MEMBER)).isEqualTo(Role.EDITOR);
    }

    @Test
    @DisplayName("default team permissions are defined for every assignable role")
    void defaultPermissionsCoverEveryAssignableRole() {
        assertThat(TeamAccessResolver.defaultPermissionsForTeamRole(Role.MANAGER)).contains(PermissionKey.VIEW_DASHBOARD);
        assertThat(TeamAccessResolver.defaultPermissionsForTeamRole(Role.EDITOR)).contains(PermissionKey.VIEW_DASHBOARD);
        assertThat(TeamAccessResolver.defaultPermissionsForTeamRole(Role.ACCOUNTANT)).contains(PermissionKey.VIEW_DASHBOARD);
    }

    @Test
    @DisplayName("platform roles have no default team permission set and must never reach this method")
    void defaultPermissionsRejectsPlatformRoles() {
        assertThatThrownBy(() -> TeamAccessResolver.defaultPermissionsForTeamRole(Role.CREATOR)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TeamAccessResolver.defaultPermissionsForTeamRole(Role.ADMIN)).isInstanceOf(IllegalStateException.class);
    }
}
