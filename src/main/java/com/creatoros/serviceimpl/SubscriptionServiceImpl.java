package com.creatoros.serviceimpl;

import java.sql.Timestamp;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatoros.dao.CreatorSubscriptionDao;
import com.creatoros.dto.subscription.SubscriptionDto;
import com.creatoros.entity.CreatorSubscription;
import com.creatoros.enums.Role;
import com.creatoros.enums.SubscriptionPlan;
import com.creatoros.exception.BadRequestException;
import com.creatoros.exception.ResourceNotFoundException;
import com.creatoros.security.SecurityUtils;
import com.creatoros.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final int TRIAL_DAYS = 30;

    private final CreatorSubscriptionDao subscriptionDao;
    private final DomainMapper           domainMapper;

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDto getCurrent(Long creatorId) {
        return domainMapper.toSubscriptionDto(requireSubscription(creatorId));
    }

    @Override
    @Transactional
    public SubscriptionDto subscribe(Long creatorId) {
        requireOwnerOrAdmin();
        CreatorSubscription subscription = requireSubscription(creatorId);
        if (subscription.getPlan() != SubscriptionPlan.SUBSCRIPTION) {
            subscription.setPlan(SubscriptionPlan.SUBSCRIPTION);
            subscription.setSubscribedAt(new Timestamp(System.currentTimeMillis()));
            subscriptionDao.save(subscription);
            log.info("Creator {} subscribed", creatorId);
        }
        return domainMapper.toSubscriptionDto(subscription);
    }

    @Override
    @Transactional
    public SubscriptionDto cancel(Long creatorId) {
        requireOwnerOrAdmin();
        CreatorSubscription subscription = requireSubscription(creatorId);
        if (subscription.getPlan() != SubscriptionPlan.TRIAL) {
            subscription.setPlan(SubscriptionPlan.TRIAL);
            subscriptionDao.save(subscription);
            log.info("Creator {} cancelled their subscription", creatorId);
        }
        return domainMapper.toSubscriptionDto(subscription);
    }

    @Override
    @Transactional
    public void createDefaultSubscription(Long creatorId) {
        if (subscriptionDao.findByCreatorId(creatorId).isPresent()) {
            return;
        }
        Timestamp trialEndsAt = new Timestamp(System.currentTimeMillis() + TRIAL_DAYS * 24L * 60 * 60 * 1000);
        subscriptionDao.save(CreatorSubscription.builder().creatorId(creatorId).plan(SubscriptionPlan.TRIAL).trialEndsAt(trialEndsAt).build());
    }

    private CreatorSubscription requireSubscription(Long creatorId) {
        return subscriptionDao.findByCreatorId(creatorId).orElseThrow(() -> ResourceNotFoundException.of("Subscription", creatorId));
    }

    private void requireOwnerOrAdmin() {
        boolean platformAdmin = SecurityUtils.currentPrincipal().map(principal -> principal.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(Role.ADMIN.asAuthority()))).orElse(false);
        if (platformAdmin || SecurityUtils.isTenantOwner()) {
            return;
        }
        throw new BadRequestException("Only the workspace owner can manage the subscription plan.", "OWNER_REQUIRED");
    }
}
