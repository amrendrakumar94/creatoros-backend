package com.creatoros.dto.deal;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UsageRightsDto(

        @PositiveOrZero(message = "Exclusivity days cannot be negative") Integer exclusivityDays,

        Boolean paidAdsAllowed,

        Boolean whitelistingAllowed,

        @Size(max = 150) String territory) {
}
