package com.creatoros.enums;

public enum Role {
    CREATOR,
    ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
