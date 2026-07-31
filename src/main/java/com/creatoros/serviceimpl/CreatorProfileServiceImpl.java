package com.creatoros.serviceimpl;

import com.creatoros.dto.BankDetailsDto;
import com.creatoros.dto.CreatorProfileDto;
import com.creatoros.dto.CurrentUserResponse;
import com.creatoros.dto.UpdateCreatorProfileRequest;
import com.creatoros.entity.BankDetails;
import com.creatoros.entity.Creator;
import com.creatoros.exception.ResourceNotFoundException;
import com.creatoros.dao.CreatorDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.function.Consumer;
import com.creatoros.service.CreatorProfileService;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreatorProfileServiceImpl implements CreatorProfileService {

    private final CreatorDao creatorDao;
    private final CreatorMapper     creatorMapper;

    @Override
    @Transactional(readOnly = true)
    public CreatorProfileDto getProfile(Long creatorId) {
        return creatorMapper.toProfileDto(requireCreator(creatorId));
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(Long creatorId) {
        Creator creator = requireCreator(creatorId);
        return new CurrentUserResponse(creator.getId(), creator.getEmail(), creator.getRole(), creator.isOnboardingCompleted(),
                creatorMapper.toProfileDto(creator));
    }

    @Override
    @Transactional
    public CreatorProfileDto updateProfile(Long creatorId, UpdateCreatorProfileRequest request) {
        Creator creator = requireCreator(creatorId);

        applyIfPresent(request.name(), creator::setName);
        applyIfPresent(request.avatar(), creator::setAvatar);
        applyIfPresent(request.phone(), creator::setPhone);
        applyIfPresent(request.creatorType(), creator::setCreatorType);
        applyIfPresent(request.tradeName(), creator::setTradeName);
        applyIfPresent(request.address(), creator::setAddress);
        applyIfPresent(request.city(), creator::setCity);
        applyIfPresent(request.pincode(), creator::setPincode);
        applyIfPresent(request.teamSize(), creator::setTeamSize);
        applyIfPresent(request.monthlyRevenueEstimate(), creator::setMonthlyRevenueEstimate);

        // GSTIN/PAN are stored uppercase so the invoice module can rely on a
        // canonical form.
        applyIfPresent(request.gstin(), value -> creator.setGstin(value.toUpperCase()));
        applyIfPresent(request.pan(), value -> creator.setPan(value.toUpperCase()));

        if (request.isGstRegistered() != null) {
            creator.setGstRegistered(request.isGstRegistered());
        }
        if (request.platforms() != null) {
            creator.setPlatforms(new LinkedHashSet<>(request.platforms()));
        }
        if (request.onboardingCompleted() != null) {
            creator.setOnboardingCompleted(request.onboardingCompleted());
        }
        if (request.bankDetails() != null) {
            applyBankDetails(creator, request.bankDetails());
        }

        creatorDao.save(creator);
        log.debug("Updated profile for creator {}", creatorId);

        return creatorMapper.toProfileDto(creator);
    }

    private void applyBankDetails(Creator creator, BankDetailsDto dto) {
        BankDetails bank = creator.getBankDetails();
        if (bank == null) {
            bank = new BankDetails();
            creator.setBankDetails(bank);
        }
        BankDetails target = bank;
        applyIfPresent(dto.bankName(), target::setBankName);
        applyIfPresent(dto.accountNumber(), target::setAccountNumber);
        applyIfPresent(dto.ifscCode(), value -> target.setIfscCode(value.toUpperCase()));
        applyIfPresent(dto.upiId(), target::setUpiId);
        applyIfPresent(dto.swiftCode(), value -> target.setSwiftCode(value.toUpperCase()));
    }

    private <T> void applyIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private Creator requireCreator(Long creatorId) {
        return creatorDao.findById(creatorId).orElseThrow(() -> ResourceNotFoundException.of("Creator", creatorId));
    }
}
