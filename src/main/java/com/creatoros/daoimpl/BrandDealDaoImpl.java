package com.creatoros.daoimpl;

import com.creatoros.entity.BrandDeal;
import com.creatoros.dao.BrandDealDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BrandDealDaoImpl extends HibernateDao implements BrandDealDao {

    @Override
    public BrandDeal save(BrandDeal deal) {
        return persistOrMerge(deal, deal.getId());
    }

    /** Cascades to deliverables and tags via the entity mapping. */
    @Override
    public void delete(BrandDeal deal) {
        removeEntity(deal);
    }

    @Override
    public List<BrandDeal> findByCreatorIdOrderByCreatedAtDesc(Long creatorId) {
        return session()
                .createSelectionQuery(
                        "from BrandDeal d where d.creator.id = :creatorId order by d.createdAt desc",
                        BrandDeal.class)
                .setParameter("creatorId", creatorId)
                .getResultList();
    }

    /** Scoped lookup: an id belonging to another creator simply is not found. */
    @Override
    public Optional<BrandDeal> findByIdAndCreatorId(Long id, Long creatorId) {
        return session()
                .createSelectionQuery(
                        "from BrandDeal d where d.id = :id and d.creator.id = :creatorId",
                        BrandDeal.class)
                .setParameter("id", id)
                .setParameter("creatorId", creatorId)
                .uniqueResultOptional();
    }

    @Override
    public long countByCreatorId(Long creatorId) {
        return session()
                .createSelectionQuery(
                        "select count(d.id) from BrandDeal d where d.creator.id = :creatorId",
                        Long.class)
                .setParameter("creatorId", creatorId)
                .getSingleResult();
    }
}
