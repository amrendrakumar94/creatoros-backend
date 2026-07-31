package com.creatoros.entity;

/**
 * Authorization role. Maps to a {@code ROLE_}-prefixed Spring Security
 * authority.
 */
public enum Role {

    /** Default role for a signed-up creator; sees only their own data. */
    CREATOR,

    /** Platform operator; backs the Admin Console view. */
    ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
