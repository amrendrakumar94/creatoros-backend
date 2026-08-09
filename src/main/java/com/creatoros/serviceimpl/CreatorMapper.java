package com.creatoros.serviceimpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.creatoros.dto.BankDetailsDto;
import com.creatoros.dto.CreatorProfileDto;
import com.creatoros.dto.CurrentUserResponse;
import com.creatoros.dto.auth.AuthResponse;
import com.creatoros.entity.BankDetails;
import com.creatoros.entity.Creator;
import com.creatoros.security.TeamAccessResolver;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreatorMapper {

    private final TeamAccessResolver teamAccessResolver;

    public CreatorProfileDto toProfileDto(Creator creator) {
        return new CreatorProfileDto(creator.getName(), creator.getHandle(), creator.getAvatar(), creator.getEmail(), creator.getPhone(),
                creator.getCreatorType(), creator.getPlatforms() == null ? List.of() : new ArrayList<>(creator.getPlatforms()),
                creator.isGstRegistered(), creator.getGstin(), creator.getPan(), creator.getTradeName(), creator.getAddress(), creator.getCity(),
                creator.getState(), creator.getStateCode(), creator.getPincode(), creator.getMonthlyRevenueEstimate(),
                toBankDetailsDto(creator.getBankDetails()), creator.getTeamSize());
    }

    public CurrentUserResponse toCurrentUserResponse(Creator creator) {
        return new CurrentUserResponse(creator.getId(), creator.getEmail(), teamAccessResolver.resolveWorkspaceRole(creator.getId()),
                teamAccessResolver.resolvePermissions(creator.getId()), creator.isOnboardingCompleted(), toProfileDto(creator));
    }

    public AuthResponse toAuthResponse(String token, Timestamp expiresAt, Creator creator) {
        return new AuthResponse(token, expiresAt, teamAccessResolver.resolveWorkspaceRole(creator.getId()),
                teamAccessResolver.resolvePermissions(creator.getId()), creator.isOnboardingCompleted(), toProfileDto(creator));
    }

    private BankDetailsDto toBankDetailsDto(BankDetails bank) {
        if (bank == null) {
            return new BankDetailsDto(null, null, null, null, null);
        }
        return new BankDetailsDto(bank.getBankName(), bank.getAccountNumber(), bank.getIfscCode(), bank.getUpiId(), bank.getSwiftCode());
    }
}
