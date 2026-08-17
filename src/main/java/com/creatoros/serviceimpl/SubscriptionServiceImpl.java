package com.creatoros.serviceimpl;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Value;
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
import com.creatoros.util.RazorpayService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final int TRIAL_DAYS = 30;

    private final CreatorSubscriptionDao subscriptionDao;
    private final DomainMapper           domainMapper;
    private final RazorpayService        razorpayService;

    @Value("${app.razorpay.subscription-amount-inr}")
    private BigDecimal subscriptionAmountInr;

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDto getCurrent(Long creatorId) {
        return domainMapper.toSubscriptionDto(requireSubscription(creatorId));
    }

    /**
     * Starts a Razorpay checkout for the subscription price. The plan itself only flips to
     * {@code SUBSCRIPTION} once the webhook confirms payment (see
     * {@link #activateFromGatewayPayment}) - never on the strength of this call returning.
     */
    @Override
    @Transactional
    public SubscriptionDto subscribe(Long creatorId) {
        requireOwnerOrAdmin();
        CreatorSubscription subscription = requireSubscription(creatorId);
        if (subscription.getPlan() == SubscriptionPlan.SUBSCRIPTION) {
            return domainMapper.toSubscriptionDto(subscription);
        }
        // Razorpay reference IDs are globally unique. Reuse an outstanding checkout when a
        // user retries instead of attempting to create a second link for this subscription.
        if (subscription.getRazorpayPaymentLinkUrl() != null) {
            return domainMapper.toSubscriptionDto(subscription);
        }

        RazorpayService.PaymentLink link = razorpayService.createPaymentLink(subscriptionAmountInr, "CreatorOS subscription",
                "subscription:" + creatorId, null, null, "/settings");
        subscription.setRazorpayPaymentLinkId(link.id());
        subscription.setRazorpayPaymentLinkUrl(link.url());
        subscriptionDao.save(subscription);

        log.info("Creator {} started a subscription checkout", creatorId);
        return domainMapper.toSubscriptionDto(subscription);
    }

    @Override
    @Transactional
    public void activateFromGatewayPayment(Long creatorId, String razorpayPaymentId) {
        CreatorSubscription subscription = requireSubscription(creatorId);
        if (razorpayPaymentId.equals(subscription.getRazorpayPaymentId())) {
            log.info("Ignoring already-processed Razorpay payment {} for creator {}", razorpayPaymentId, creatorId);
            return;
        }

        subscription.setPlan(SubscriptionPlan.SUBSCRIPTION);
        subscription.setSubscribedAt(new Timestamp(System.currentTimeMillis()));
        subscription.setRazorpayPaymentId(razorpayPaymentId);
        subscription.setRazorpayPaymentLinkId(null);
        subscription.setRazorpayPaymentLinkUrl(null);
        subscriptionDao.save(subscription);

        log.info("Activated subscription for creator {} via Razorpay payment {}", creatorId, razorpayPaymentId);
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
