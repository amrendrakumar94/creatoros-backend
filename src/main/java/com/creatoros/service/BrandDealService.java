package com.creatoros.service;

import com.creatoros.dto.deal.BrandDealDto;
import com.creatoros.dto.deal.BrandDealRequest;
import com.creatoros.entity.DealStage;

import java.util.List;

public interface BrandDealService {

    List<BrandDealDto> listForCreator(Long creatorId);

    BrandDealDto get(Long creatorId, Long dealId);

    /** Assigns the next BD-YYYY-NN number for this creator. */
    BrandDealDto create(Long creatorId, BrandDealRequest request);

    BrandDealDto update(Long creatorId, Long dealId, BrandDealRequest request);

    BrandDealDto updateStage(Long creatorId, Long dealId, DealStage stage);

    void delete(Long creatorId, Long dealId);
}
