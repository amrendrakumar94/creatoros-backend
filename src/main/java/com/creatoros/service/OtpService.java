package com.creatoros.service;

import com.creatoros.entity.Creator;
import com.creatoros.entity.OtpPurpose;

public interface OtpService {

    /**
     * Invalidates any outstanding code for this creator/purpose, issues a fresh
     * one, and hands it to the configured {@link OtpSender}.
     */
    void issue(Creator creator, OtpPurpose purpose);

    /**
     * Redeems a code. On success the token is marked consumed and cannot be
     * reused.
     *
     * @throws com.creatoros.exception.BadRequestException if the code is wrong,
     *             expired, already used, or the attempt cap is exceeded
     */
    void verify(Creator creator, OtpPurpose purpose, String code);
}
