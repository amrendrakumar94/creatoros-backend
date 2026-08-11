package com.creatoros.dao;

import java.util.Optional;

import com.creatoros.entity.CreatorSubscription;

public interface CreatorSubscriptionDao {

    CreatorSubscription save(CreatorSubscription subscription);

    Optional<CreatorSubscription> findByCreatorId(Long creatorId);
}
