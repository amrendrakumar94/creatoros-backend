package com.creatoros.entity;

public enum Role {
    CREATOR,
    ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
