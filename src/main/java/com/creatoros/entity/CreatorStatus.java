package com.creatoros.entity;

/** Lifecycle of a creator account. */
public enum CreatorStatus {

    /**
     * Registered but the signup OTP has not been verified yet. Cannot log in.
     */
    PENDING,

    /** Verified and able to authenticate. */
    ACTIVE,

    /** Administratively disabled. */
    SUSPENDED
}
