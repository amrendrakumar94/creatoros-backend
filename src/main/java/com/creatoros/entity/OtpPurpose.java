package com.creatoros.entity;

/** What a one-time code is allowed to authorize. */
public enum OtpPurpose {

    /** Verifies a new account and flips it from PENDING to ACTIVE. */
    SIGNUP,

    /** Authorizes setting a new password without knowing the old one. */
    PASSWORD_RESET
}
