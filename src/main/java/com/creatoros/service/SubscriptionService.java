package com.creatoros.service;

import com.creatoros.dto.subscription.SubscriptionDto;

public interface SubscriptionService {

    SubscriptionDto getCurrent(Long creatorId);

    SubscriptionDto subscribe(Long creatorId);

    SubscriptionDto cancel(Long creatorId);

    void createDefaultSubscription(Long creatorId);
}
