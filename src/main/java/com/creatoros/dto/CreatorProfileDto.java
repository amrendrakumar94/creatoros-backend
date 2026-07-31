package com.creatoros.dto;

import com.creatoros.entity.CreatorType;
import com.creatoros.entity.PlatformType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Serializes to exactly the {@code CreatorProfile} interface in the frontend's
 * src/types/creatorOS.ts, so the UI consumes it with no type changes.
 *
 * <p>
 * {@code creatorType} and {@code platforms} render as their display labels
 * ("Finance &amp; Investing", "X (Twitter)") via the enums' {@code @JsonValue}.
 */
public record CreatorProfileDto(String name, String handle, String avatar, String email, String phone, CreatorType creatorType,
        List<PlatformType> platforms, boolean isGstRegistered, String gstin, String pan, String tradeName, String address, String city,
        String pincode, BigDecimal monthlyRevenueEstimate, BankDetailsDto bankDetails, String teamSize) {
}
