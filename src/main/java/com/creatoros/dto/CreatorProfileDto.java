package com.creatoros.dto;

import java.math.BigDecimal;
import java.util.List;

import com.creatoros.entity.CreatorType;
import com.creatoros.entity.PlatformType;

public record CreatorProfileDto(

        String name,

        String handle,

        String avatar,

        String email,

        String phone,

        CreatorType creatorType,

        List<PlatformType> platforms,

        boolean isGstRegistered,

        String gstin,

        String pan,

        String tradeName,

        String address,

        String city,

        String pincode,

        BigDecimal monthlyRevenueEstimate,

        BankDetailsDto bankDetails,

        String teamSize) {
}
