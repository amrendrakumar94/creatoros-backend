package com.creatoros.dao;

import com.creatoros.entity.BrandDeal;

import java.util.List;
import java.util.Optional;

public interface BrandDealDao {

    BrandDeal save(BrandDeal deal);

    /** Cascades to deliverables and tags. */
    void delete(BrandDeal deal);

    List<BrandDeal> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    /** Scoped lookup: an id belonging to another creator simply is not found. */
    Optional<BrandDeal> findByIdAndCreatorId(Long id, Long creatorId);

    long countByCreatorId(Long creatorId);
}
