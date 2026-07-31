package com.creatoros.repository;

import com.creatoros.entity.BrandDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandDealRepository extends JpaRepository<BrandDeal, Long> {

    List<BrandDeal> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    /** Scoped lookup: an id belonging to another creator simply is not found. */
    Optional<BrandDeal> findByIdAndCreatorId(Long id, Long creatorId);

    long countByCreatorId(Long creatorId);
}
