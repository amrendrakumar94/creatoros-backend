package com.creatoros.service;

import com.creatoros.config.AppProperties;
import com.creatoros.entity.Creator;
import com.creatoros.entity.OtpPurpose;
import com.creatoros.entity.OtpToken;
import com.creatoros.exception.BadRequestException;
import com.creatoros.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpTokenRepository  otpTokenRepository;
    private final OtpSender           otpSender;
    private final AppProperties       appProperties;

    @Override
    @Transactional
    public void issue(Creator creator, OtpPurpose purpose) {
        // Only the most recently issued code may be redeemable.
        otpTokenRepository.consumeOutstanding(creator, purpose, Instant.now());

        String code = generateCode();
        OtpToken token = OtpToken.builder().creator(creator).code(code).purpose(purpose)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(appProperties.getOtp().getTtlMinutes()))).attempts(0).build();

        otpTokenRepository.save(token);
        otpSender.send(creator, code, purpose);
    }

    @Override
    @Transactional
    public void verify(Creator creator, OtpPurpose purpose, String code) {
        Optional<OtpToken> maybeToken = otpTokenRepository.findFirstByCreatorAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(creator, purpose);

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
            otpTokenRepository.save(token);

            int remaining = maxAttempts - token.getAttempts();
            log.warn("Incorrect OTP for creator {} ({} attempts remaining)", creator.getId(), remaining);

            if (remaining <= 0) {
                throw new BadRequestException("Too many incorrect attempts. Request a new code.", "OTP_ATTEMPTS_EXCEEDED");
            }
            throw new BadRequestException("Incorrect verification code. " + remaining + " attempt(s) remaining.", "OTP_INVALID");
        }

        token.setConsumedAt(Instant.now());
        otpTokenRepository.save(token);
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
