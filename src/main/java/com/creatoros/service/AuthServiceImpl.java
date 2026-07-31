package com.creatoros.service;

import com.creatoros.dto.auth.AuthResponse;
import com.creatoros.dto.auth.LoginRequest;
import com.creatoros.dto.auth.ResetPasswordRequest;
import com.creatoros.dto.auth.SignupRequest;
import com.creatoros.dto.auth.VerifyOtpRequest;
import com.creatoros.entity.BankDetails;
import com.creatoros.entity.Creator;
import com.creatoros.entity.CreatorStatus;
import com.creatoros.entity.OtpPurpose;
import com.creatoros.entity.Role;
import com.creatoros.exception.AccountNotVerifiedException;
import com.creatoros.exception.BadRequestException;
import com.creatoros.exception.InvalidCredentialsException;
import com.creatoros.repository.CreatorRepository;
import com.creatoros.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CreatorRepository creatorRepository;
    private final OtpService        otpService;
    private final PasswordEncoder   passwordEncoder;
    private final JwtService        jwtService;
    private final CreatorMapper     creatorMapper;

    @Override
    @Transactional
    public void signup(SignupRequest request) {
        String email = normalizeEmail(request.email());

        Optional<Creator> existing = creatorRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            Creator creator = existing.get();
            // A half-finished signup should not be a dead end - refresh the
            // details and re-send.
            if (creator.getStatus() == CreatorStatus.PENDING) {
                creator.setName(request.name());
                creator.setPhone(request.phone());
                creator.setPasswordHash(passwordEncoder.encode(request.password()));
                creatorRepository.save(creator);
                otpService.issue(creator, OtpPurpose.SIGNUP);
                return;
            }
            throw new BadRequestException("An account with this email already exists. Sign in instead.", "EMAIL_TAKEN");
        }

        Creator creator = Creator.builder().email(email).passwordHash(passwordEncoder.encode(request.password())).status(CreatorStatus.PENDING)
                .role(Role.CREATOR).name(request.name()).handle(generateUniqueHandle(request.name(), email)).phone(request.phone())
                .platforms(new LinkedHashSet<>()).gstRegistered(false).monthlyRevenueEstimate(BigDecimal.ZERO).bankDetails(new BankDetails())
                .onboardingCompleted(false).build();

        creatorRepository.save(creator);
        otpService.issue(creator, OtpPurpose.SIGNUP);
        log.info("Registered creator {} ({})", creator.getId(), email);
    }

    @Override
    @Transactional
    public AuthResponse verifySignupOtp(VerifyOtpRequest request) {
        Creator creator = requireCreator(request.email());

        if (creator.getStatus() == CreatorStatus.SUSPENDED) {
            throw new BadRequestException("This account has been suspended.", "ACCOUNT_SUSPENDED");
        }

        otpService.verify(creator, OtpPurpose.SIGNUP, request.code());

        creator.setStatus(CreatorStatus.ACTIVE);
        creatorRepository.save(creator);

        return buildAuthResponse(creator);
    }

    @Override
    @Transactional
    public void resendSignupOtp(String email) {
        Creator creator = requireCreator(email);
        if (creator.getStatus() == CreatorStatus.ACTIVE) {
            throw new BadRequestException("This account is already verified. Sign in instead.", "ALREADY_VERIFIED");
        }
        otpService.issue(creator, OtpPurpose.SIGNUP);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Creator creator = creatorRepository.findByEmailIgnoreCase(normalizeEmail(request.email())).orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), creator.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (creator.getStatus() == CreatorStatus.PENDING) {
            throw new AccountNotVerifiedException(creator.getEmail());
        }
        if (creator.getStatus() == CreatorStatus.SUSPENDED) {
            throw new BadRequestException("This account has been suspended.", "ACCOUNT_SUSPENDED");
        }

        return buildAuthResponse(creator);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        // Never reveal whether the address is registered.
        creatorRepository.findByEmailIgnoreCase(normalizeEmail(email)).ifPresentOrElse(
                creator -> otpService.issue(creator, OtpPurpose.PASSWORD_RESET),
                () -> log.info("Password reset requested for unregistered email {}", email));
    }

    @Override
    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        Creator creator = requireCreator(request.email());

        otpService.verify(creator, OtpPurpose.PASSWORD_RESET, request.code());

        creator.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        // Completing a reset proves control of the mailbox, so an unverified
        // account becomes active.
        if (creator.getStatus() == CreatorStatus.PENDING) {
            creator.setStatus(CreatorStatus.ACTIVE);
        }
        creatorRepository.save(creator);

        return buildAuthResponse(creator);
    }

    private AuthResponse buildAuthResponse(Creator creator) {
        return new AuthResponse(jwtService.generateToken(creator), jwtService.expiryFromNow(), creator.isOnboardingCompleted(),
                creatorMapper.toProfileDto(creator));
    }

    private Creator requireCreator(String email) {
        return creatorRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new BadRequestException("No account found for this email.", "ACCOUNT_NOT_FOUND"));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Builds a stable public handle from the creator's name, falling back to
     * the email local part, and disambiguates with a numeric suffix on
     * collision.
     */
    private String generateUniqueHandle(String name, String email) {
        String base = slugify(name);
        if (base.isEmpty()) {
            base = slugify(email.split("@")[0]);
        }
        if (base.isEmpty()) {
            base = "creator";
        }
        base = base.substring(0, Math.min(base.length(), 60));

        String candidate = "@" + base;
        int suffix = 1;
        while (creatorRepository.existsByHandle(candidate)) {
            candidate = "@" + base + suffix++;
        }
        return candidate;
    }

    private String slugify(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
