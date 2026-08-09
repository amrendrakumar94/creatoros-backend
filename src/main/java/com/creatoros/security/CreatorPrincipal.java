package com.creatoros.security;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.creatoros.entity.Creator;
import com.creatoros.enums.CreatorStatus;
import com.creatoros.enums.PermissionKey;

import lombok.Getter;

@Getter
public class CreatorPrincipal implements UserDetails {

    private final Long                                    creatorId;
    private final String                                  email;
    private final String                                  passwordHash;
    private final CreatorStatus                           status;
    private final Collection< ? extends GrantedAuthority> authorities;
    private final Long                                    tenantId;
    private final Set<PermissionKey>                      permissions;

    public CreatorPrincipal(Creator creator, Long tenantId, Set<PermissionKey> permissions) {
        this.creatorId = creator.getId();
        this.email = creator.getEmail();
        this.passwordHash = creator.getPasswordHash();
        this.status = creator.getStatus();
        this.authorities = List.of(new SimpleGrantedAuthority(creator.getRole().asAuthority()));
        this.tenantId = tenantId;
        this.permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return status == CreatorStatus.ACTIVE;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != CreatorStatus.SUSPENDED;
    }
}
