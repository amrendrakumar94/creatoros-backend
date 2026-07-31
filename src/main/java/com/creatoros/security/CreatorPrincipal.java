package com.creatoros.security;

import com.creatoros.entity.Creator;
import com.creatoros.entity.CreatorStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * The authenticated creator, carried on the SecurityContext for the life of a
 * request.
 */
@Getter
public class CreatorPrincipal implements UserDetails {

    private final Long                                    creatorId;
    private final String                                  email;
    private final String                                  passwordHash;
    private final CreatorStatus                           status;
    private final Collection< ? extends GrantedAuthority> authorities;

    public CreatorPrincipal(Creator creator) {
        this.creatorId = creator.getId();
        this.email = creator.getEmail();
        this.passwordHash = creator.getPasswordHash();
        this.status = creator.getStatus();
        this.authorities = List.of(new SimpleGrantedAuthority(creator.getRole().asAuthority()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    /**
     * A PENDING account has not verified its signup OTP and must not hold a
     * session.
     */
    @Override
    public boolean isEnabled() {
        return status == CreatorStatus.ACTIVE;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != CreatorStatus.SUSPENDED;
    }
}
