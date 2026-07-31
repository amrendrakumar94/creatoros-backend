package com.creatoros.dao;

import java.time.Instant;
import java.util.Optional;

import com.creatoros.entity.Creator;
import com.creatoros.entity.OtpPurpose;
import com.creatoros.entity.OtpToken;

public interface OtpTokenDao {

    OtpToken save(OtpToken token);

    Optional<OtpToken> findFirstByCreatorAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(Creator creator, OtpPurpose purpose);

    int consumeOutstanding(Creator creator, OtpPurpose purpose, Instant now);
}
