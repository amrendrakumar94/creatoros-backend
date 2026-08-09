package com.creatoros.security;

import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatoros.dao.CreatorDao;
import com.creatoros.entity.Creator;
import com.creatoros.entity.TeamMember;
import com.creatoros.enums.PermissionKey;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreatorUserDetailsService implements UserDetailsService {

    private final CreatorDao         creatorDao;
    private final TeamAccessResolver teamAccessResolver;

    @Override
    @Transactional(readOnly = true)
    public CreatorPrincipal loadUserByUsername(String email) throws UsernameNotFoundException {
        Creator creator = creatorDao.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No creator with email " + email));

        Optional<TeamMember> membership = teamAccessResolver.activeMembership(creator.getId());
        Long tenantId = membership.map(TeamMember::getCreatorId).orElse(creator.getId());
        Set<PermissionKey> permissions = membership.map(TeamMember::getPermissions).orElseGet(() -> Set.of(PermissionKey.values()));

        return new CreatorPrincipal(creator, tenantId, permissions);
    }
}
