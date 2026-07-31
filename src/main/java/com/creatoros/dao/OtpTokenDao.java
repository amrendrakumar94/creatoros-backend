package com.creatoros.dao;

import com.creatoros.entity.Creator;
import com.creatoros.entity.OtpPurpose;
import com.creatoros.entity.OtpToken;

import java.time.Instant;
import java.util.Optional;

public interface OtpTokenDao {

    OtpToken save(OtpToken token);

    /** The newest still-redeemable code for this creator and purpose. */
    Optional<OtpToken> findFirstByCreatorAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(Creator creator, OtpPurpose purpose);

    /**
     * Burns every outstanding code for a creator/purpose before a new one is
     * issued, so only the most recently sent code is ever valid.
     *
     * <p>
     * This is a bulk update: it flushes first and clears the session
     * afterwards, so entities loaded before the call are detached on return.
     */
    int consumeOutstanding(Creator creator, OtpPurpose purpose, Instant now);
}
