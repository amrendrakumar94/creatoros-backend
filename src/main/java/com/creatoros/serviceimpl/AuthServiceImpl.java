package com.creatoros.serviceimpl;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatoros.dao.CreatorDao;
import com.creatoros.dao.TeamInvitationDao;
import com.creatoros.dto.auth.AuthResponse;
import com.creatoros.dto.auth.LoginRequest;
import com.creatoros.dto.auth.SignupRequest;
import com.creatoros.entity.BankDetails;
import com.creatoros.entity.Creator;
import com.creatoros.enums.CreatorStatus;
import com.creatoros.enums.Role;
import com.creatoros.exception.BadRequestException;
import com.creatoros.exception.InvalidCredentialsException;
import com.creatoros.security.JwtService;
import com.creatoros.service.AuthService;
import com.creatoros.service.SubscriptionService;
import com.creatoros.service.TeamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CreatorDao       creatorDao;
    private final TeamInvitationDao teamInvitationDao;
    private final PasswordEncoder   passwordEncoder;
    private final JwtService       jwtService;
    private final CreatorMapper    creatorMapper;
    private final TeamService      teamService;
    private final SubscriptionService subscriptionService;

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());

        if (creatorDao.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("An account with this email already exists. Sign in instead.", "EMAIL_TAKEN");
        }

        Creator creator = Creator.builder().email(email).passwordHash(passwordEncoder.encode(request.password()))
                .status(CreatorStatus.ACTIVE).role(Role.CREATOR).name(request.name())
                .handle(generateUniqueHandle(request.name(), email)).phone(request.phone()).platforms(new LinkedHashSet<>())
                .gstRegistered(false).monthlyRevenueEstimate(BigDecimal.ZERO).bankDetails(new BankDetails())
                .onboardingCompleted(false).build();

        creatorDao.save(creator);
        subscriptionService.createDefaultSubscription(creator.getId());
        teamInvitationDao.findFirstByEmailIgnoreCaseAndRevokedFalseAndAcceptedAtIsNull(email).ifPresent(invitation -> {
            teamService.acceptInvitation(invitation.getInviteToken(), creator.getId());
        });
        log.info("Registered creator {} ({})", creator.getId(), email);

        return buildAuthResponse(creator);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Creator creator = creatorDao.findByEmailIgnoreCase(normalizeEmail(request.email())).orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), creator.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (creator.getStatus() == CreatorStatus.SUSPENDED) {
            throw new BadRequestException("This account has been suspended.", "ACCOUNT_SUSPENDED");
        }

        return buildAuthResponse(creator);
    }

    private AuthResponse buildAuthResponse(Creator creator) {
        return creatorMapper.toAuthResponse(jwtService.generateToken(creator), jwtService.expiryFromNow(), creator);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

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
        while (creatorDao.existsByHandle(candidate)) {
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
