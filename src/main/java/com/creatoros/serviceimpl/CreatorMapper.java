package com.creatoros.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.creatoros.dto.BankDetailsDto;
import com.creatoros.dto.CreatorProfileDto;
import com.creatoros.entity.BankDetails;
import com.creatoros.entity.Creator;

@Component
public class CreatorMapper {

    public CreatorProfileDto toProfileDto(Creator creator) {
        return new CreatorProfileDto(creator.getName(), creator.getHandle(), creator.getAvatar(), creator.getEmail(), creator.getPhone(),
                creator.getCreatorType(), creator.getPlatforms() == null ? List.of() : new ArrayList<>(creator.getPlatforms()),
                creator.isGstRegistered(), creator.getGstin(), creator.getPan(), creator.getTradeName(), creator.getAddress(), creator.getCity(),
                creator.getPincode(), creator.getMonthlyRevenueEstimate(), toBankDetailsDto(creator.getBankDetails()), creator.getTeamSize());
    }

    private BankDetailsDto toBankDetailsDto(BankDetails bank) {
        if (bank == null) {
            return new BankDetailsDto(null, null, null, null, null);
        }
        return new BankDetailsDto(bank.getBankName(), bank.getAccountNumber(), bank.getIfscCode(), bank.getUpiId(), bank.getSwiftCode());
    }
}
