package com.creatoros.daoimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.creatoros.dao.BrandDealDao;
import com.creatoros.entity.BrandDeal;

@Repository
public class BrandDealDaoImpl extends HibernateDao implements BrandDealDao {

    @Override
    public BrandDeal save(BrandDeal deal) {
        return persistOrMerge(deal, deal.getId());
    }

    @Override
    public void delete(BrandDeal deal) {
        removeEntity(deal);
    }

    @Override
    public List<BrandDeal> findByCreatorIdOrderByCreatedAtDesc(Long creatorId) {
        return session().createSelectionQuery("from BrandDeal d where d.creator.id = :creatorId order by d.createdAt desc", BrandDeal.class)
                .setParameter("creatorId", creatorId).getResultList();
    }

    @Override
    public Optional<BrandDeal> findByIdAndCreatorId(Long id, Long creatorId) {
        return session().createSelectionQuery("from BrandDeal d where d.id = :id and d.creator.id = :creatorId", BrandDeal.class)
                .setParameter("id", id).setParameter("creatorId", creatorId).uniqueResultOptional();
    }

    @Override
    public long countByCreatorId(Long creatorId) {
        return session().createSelectionQuery("select count(d.id) from BrandDeal d where d.creator.id = :creatorId", Long.class)
                .setParameter("creatorId", creatorId).getSingleResult();
    }
}
