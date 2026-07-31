package com.creatoros.repository;

import com.creatoros.entity.Creator;
import com.creatoros.entity.OtpPurpose;
import com.creatoros.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    /** The newest still-redeemable code for this creator and purpose. */
    Optional<OtpToken> findFirstByCreatorAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            Creator creator, OtpPurpose purpose);

    /**
     * Burns every outstanding code for a creator/purpose before a new one is issued, so only the
     * most recently sent code is ever valid.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE OtpToken o
               SET o.consumedAt = :now
             WHERE o.creator = :creator
               AND o.purpose = :purpose
               AND o.consumedAt IS NULL
            """)
    int consumeOutstanding(@Param("creator") Creator creator,
                           @Param("purpose") OtpPurpose purpose,
                           @Param("now") Instant now);
}
