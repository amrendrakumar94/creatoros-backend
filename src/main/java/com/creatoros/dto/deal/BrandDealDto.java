package com.creatoros.dto.deal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.creatoros.enums.DealStage;
import com.creatoros.enums.PaymentTerms;
import com.creatoros.enums.PlatformType;

public record BrandDealDto(

        String id,

        String dealNumber,

        String brandName,

        String brandLogo,

        String category,

        String contactPerson,

        String contactEmail,

        String contactPhone,

        BigDecimal amount,

        DealStage stage,

        PlatformType platform,

        String campaignTitle,

        LocalDate startDate,

        LocalDate endDate, List<DeliverableItemDto> deliverables,

        UsageRightsDto usageRights,

        String negotiationNotes,

        PaymentTerms paymentTerms,

        List<String> tags) {
}
