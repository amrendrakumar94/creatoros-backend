package com.creatoros.dao;

import java.util.List;
import java.util.Optional;

import com.creatoros.entity.BrandDeal;

public interface BrandDealDao {

    BrandDeal save(BrandDeal deal);

    void delete(BrandDeal deal);

    List<BrandDeal> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    Optional<BrandDeal> findByIdAndCreatorId(Long id, Long creatorId);

}
