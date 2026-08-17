package com.creatoros.service;

import com.creatoros.dto.subscription.SubscriptionDto;

public interface SubscriptionService {

    SubscriptionDto getCurrent(Long creatorId);

    /** Starts (or restarts) a Razorpay checkout for the subscription price - does not activate the plan. */
    SubscriptionDto subscribe(Long creatorId);

    SubscriptionDto cancel(Long creatorId);

    void createDefaultSubscription(Long creatorId);

    /**
     * Activates the plan once Razorpay confirms payment. Deliberately not gated by
     * {@code requireOwnerOrAdmin} - the webhook that calls this has no acting tenant, only the
     * creator id encoded in the payment link's reference; its own signature check is what stands
     * in for authentication here.
     */
    void activateFromGatewayPayment(Long creatorId, String razorpayPaymentId);
}
