package com.creatoros.serviceimpl;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatoros.config.AppProperties;
import com.creatoros.dao.OtpTokenDao;
import com.creatoros.entity.Creator;
import com.creatoros.enums.OtpPurpose;
import com.creatoros.entity.OtpToken;
import com.creatoros.exception.BadRequestException;
import com.creatoros.service.OtpSender;
import com.creatoros.service.OtpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpTokenDao         otpTokenDao;
    private final OtpSender           otpSender;
    private final AppProperties       appProperties;

    @Override
    @Transactional
    public void issue(Creator creator, OtpPurpose purpose) {
        otpTokenDao.consumeOutstanding(creator, purpose, Instant.now());

        String code = generateCode();
        OtpToken token = OtpToken.builder().creator(creator).code(code).purpose(purpose)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(appProperties.getOtp().getTtlMinutes()))).attempts(0).build();

        otpTokenDao.save(token);
        otpSender.send(creator, code, purpose);
    }

    @Override
    @Transactional
    public void verify(Creator creator, OtpPurpose purpose, String code) {
        Optional<OtpToken> maybeToken = otpTokenDao.findFirstByCreatorAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(creator, purpose);

        OtpToken token = maybeToken
                .orElseThrow(() -> new BadRequestException("No verification code is pending. Request a new one.", "OTP_NOT_FOUND"));

        if (token.isExpired()) {
            throw new BadRequestException("This verification code has expired. Request a new one.", "OTP_EXPIRED");
        }

        int maxAttempts = appProperties.getOtp().getMaxAttempts();
        if (token.getAttempts() >= maxAttempts) {
            throw new BadRequestException("Too many incorrect attempts. Request a new code.", "OTP_ATTEMPTS_EXCEEDED");
        }

        if (!token.getCode().equals(code)) {
            token.setAttempts(token.getAttempts() + 1);
            otpTokenDao.save(token);

            int remaining = maxAttempts - token.getAttempts();
            log.warn("Incorrect OTP for creator {} ({} attempts remaining)", creator.getId(), remaining);

            if (remaining <= 0) {
                throw new BadRequestException("Too many incorrect attempts. Request a new code.", "OTP_ATTEMPTS_EXCEEDED");
            }
            throw new BadRequestException("Incorrect verification code. " + remaining + " attempt(s) remaining.", "OTP_INVALID");
        }

        token.setConsumedAt(Instant.now());
        otpTokenDao.save(token);
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
