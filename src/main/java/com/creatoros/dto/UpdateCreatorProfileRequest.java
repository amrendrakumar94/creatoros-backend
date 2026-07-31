package com.creatoros.dto;

import com.creatoros.entity.CreatorType;
import com.creatoros.entity.PlatformType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Partial profile update. Every field is optional - a {@code null} means "leave
 * unchanged", which is what lets both the 4-step onboarding flow and the
 * Settings screen post to the same endpoint.
 *
 * <p>
 * Consequence: a field cannot be cleared by sending null. Send an empty string
 * instead.
 */
public record UpdateCreatorProfileRequest(

        @Size(max = 150, message = "Name must be at most 150 characters") String name,

        @Size(max = 500) String avatar,

        @Size(max = 30) String phone,

        CreatorType creatorType,

        List<PlatformType> platforms,

        Boolean isGstRegistered,

        // Case-insensitive: the service canonicalises these to uppercase on
        // save.
        @Pattern(regexp = "^$|^(?i)[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z][Z][0-9A-Z]$", message = "GSTIN must be 15 characters in the standard GSTIN format") String gstin,

        @Pattern(regexp = "^$|^(?i)[A-Z]{5}[0-9]{4}[A-Z]$", message = "PAN must be 10 characters, e.g. ABCDE1234F") String pan,

        @Size(max = 200) String tradeName,

        @Size(max = 500) String address,

        @Size(max = 120) String city,

        @Pattern(regexp = "^$|^[1-9][0-9]{5}$", message = "Pincode must be 6 digits") String pincode,

        @PositiveOrZero(message = "Monthly revenue estimate cannot be negative") BigDecimal monthlyRevenueEstimate,

        @Valid BankDetailsDto bankDetails,

        @Size(max = 50) String teamSize,

        /** Set by the onboarding flow on its final step. */
        Boolean onboardingCompleted) {
}
