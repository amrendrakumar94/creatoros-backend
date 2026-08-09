package com.creatoros.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creatoros.dao.CreatorDao;
import com.creatoros.entity.Creator;
import com.creatoros.entity.TeamMember;
import com.creatoros.enums.CreatorStatus;
import com.creatoros.enums.PermissionKey;
import com.creatoros.enums.Role;

@ExtendWith(MockitoExtension.class)
class CreatorUserDetailsServiceTest {

    private static final Long   OWNER  = 1L;
    private static final Long   MEMBER = 2L;
    private static final String EMAIL  = "person@example.com";

    @Mock
    private CreatorDao creatorDao;

    @Mock
    private TeamAccessResolver teamAccessResolver;

    @InjectMocks
    private CreatorUserDetailsService service;

    @Test
    @DisplayName("a creator with no active membership resolves to their own tenant and full permissions")
    void resolvesOwnWorkspaceWithoutMembership() {
        Creator creator = creator(OWNER, EMAIL);
        when(creatorDao.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(creator));
        when(teamAccessResolver.activeMembership(OWNER)).thenReturn(Optional.empty());

        CreatorPrincipal principal = service.loadUserByUsername(EMAIL);

        assertThat(principal.getCreatorId()).isEqualTo(OWNER);
        assertThat(principal.getTenantId()).isEqualTo(OWNER);
        assertThat(principal.getPermissions()).containsExactlyInAnyOrder(PermissionKey.values());
        verify(teamAccessResolver, times(1)).activeMembership(OWNER);
    }

    @Test
    @DisplayName("an active team member resolves to the owner's tenant and their granted permissions")
    void resolvesOwnerWorkspaceWithActiveMembership() {
        Creator creator = creator(MEMBER, EMAIL);
        Set<PermissionKey> granted = Set.of(PermissionKey.VIEW_DASHBOARD, PermissionKey.MANAGE_DEALS);
        TeamMember membership = TeamMember.builder().creatorId(OWNER).memberCreatorId(MEMBER).role(Role.MANAGER).active(true)
                .permissions(granted).build();
        when(creatorDao.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(creator));
        when(teamAccessResolver.activeMembership(MEMBER)).thenReturn(Optional.of(membership));

        CreatorPrincipal principal = service.loadUserByUsername(EMAIL);

        assertThat(principal.getCreatorId()).isEqualTo(MEMBER);
        assertThat(principal.getTenantId()).isEqualTo(OWNER);
        assertThat(principal.getPermissions()).containsExactlyInAnyOrderElementsOf(granted);
        verify(teamAccessResolver, times(1)).activeMembership(MEMBER);
    }

    private static Creator creator(Long id, String email) {
        return Creator.builder().id(id).email(email).passwordHash("hash").status(CreatorStatus.ACTIVE).role(Role.CREATOR).name("Test")
                .handle("test-" + id).build();
    }
}
