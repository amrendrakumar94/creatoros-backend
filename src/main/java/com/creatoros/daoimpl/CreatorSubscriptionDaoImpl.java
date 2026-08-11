package com.creatoros.daoimpl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.creatoros.dao.CreatorSubscriptionDao;
import com.creatoros.entity.CreatorSubscription;

@Repository
public class CreatorSubscriptionDaoImpl extends HibernateDao implements CreatorSubscriptionDao {

    @Override
    public CreatorSubscription save(CreatorSubscription subscription) {
        return persistOrMerge(subscription, subscription.getId());
    }

    @Override
    public Optional<CreatorSubscription> findByCreatorId(Long creatorId) {
        return session().createSelectionQuery("from CreatorSubscription s where s.creatorId = :creatorId", CreatorSubscription.class)
                .setParameter("creatorId", creatorId).uniqueResultOptional();
    }
}
