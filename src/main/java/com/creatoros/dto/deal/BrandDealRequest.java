package com.creatoros.dto.deal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.creatoros.entity.DealStage;
import com.creatoros.entity.PaymentTerms;
import com.creatoros.entity.PlatformType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record BrandDealRequest(

        @NotBlank(message = "Brand name is required") @Size(max = 200) String brandName,

        @Size(max = 20) String brandLogo,

        @Size(max = 120) String category,

        @Size(max = 150) String contactPerson,

        // Blank is allowed: the contact may not be known when a lead is first
        // logged.
        @Email(message = "Enter a valid contact email") @Size(max = 255) String contactEmail,

        @Size(max = 30) String contactPhone,

        @NotNull(message = "Deal value is required") @PositiveOrZero(message = "Deal value cannot be negative") BigDecimal amount,

        @NotNull(message = "Pipeline stage is required") DealStage stage,

        @NotNull(message = "Platform is required") PlatformType platform,

        @Size(max = 300) String campaignTitle,

        LocalDate startDate,

        LocalDate endDate,

        @Valid List<DeliverableItemDto> deliverables,

        @Valid UsageRightsDto usageRights,

        String negotiationNotes,

        PaymentTerms paymentTerms,

        List<String> tags) {
}
