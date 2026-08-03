package com.creatoros.serviceimpl;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatoros.dao.BrandDealDao;
import com.creatoros.dao.CreatorDao;
import com.creatoros.dto.deal.BrandDealDto;
import com.creatoros.dto.deal.BrandDealRequest;
import com.creatoros.dto.deal.DeliverableItemDto;
import com.creatoros.dto.deal.UsageRightsDto;
import com.creatoros.entity.BrandDeal;
import com.creatoros.entity.Creator;
import com.creatoros.enums.DealStage;
import com.creatoros.entity.DeliverableItem;
import com.creatoros.enums.DeliverableStatus;
import com.creatoros.enums.PaymentTerms;
import com.creatoros.entity.UsageRights;
import com.creatoros.exception.ResourceNotFoundException;
import com.creatoros.service.BrandDealService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrandDealServiceImpl implements BrandDealService {

    private final BrandDealDao        brandDealDao;
    private final CreatorDao          creatorDao;
    private final DomainMapper        domainMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BrandDealDto> listForCreator(Long creatorId) {
        return brandDealDao.findByCreatorIdOrderByCreatedAtDesc(creatorId).stream().map(domainMapper::toDealDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BrandDealDto get(Long creatorId, Long dealId) {
        return domainMapper.toDealDto(requireDeal(creatorId, dealId));
    }

    @Override
    @Transactional
    public BrandDealDto create(Long creatorId, BrandDealRequest request) {
        Creator creator = requireCreator(creatorId);

        BrandDeal deal = BrandDeal.builder().creator(creator).dealNumber(nextDealNumber(creatorId)).stage(request.stage())
                .platform(request.platform()).amount(request.amount())
                .paymentTerms(request.paymentTerms() == null ? PaymentTerms.NET_30 : request.paymentTerms()).build();

        applyRequest(deal, request);
        brandDealDao.save(deal);

        log.info("Created deal {} for creator {}", deal.getDealNumber(), creatorId);
        return domainMapper.toDealDto(deal);
    }

    @Override
    @Transactional
    public BrandDealDto update(Long creatorId, Long dealId, BrandDealRequest request) {
        BrandDeal deal = requireDeal(creatorId, dealId);

        deal.setStage(request.stage());
        deal.setPlatform(request.platform());
        deal.setAmount(request.amount());
        if (request.paymentTerms() != null) {
            deal.setPaymentTerms(request.paymentTerms());
        }
        applyRequest(deal, request);

        return domainMapper.toDealDto(brandDealDao.save(deal));
    }

    @Override
    @Transactional
    public BrandDealDto updateStage(Long creatorId, Long dealId, DealStage stage) {
        BrandDeal deal = requireDeal(creatorId, dealId);
        deal.setStage(stage);
        brandDealDao.save(deal);
        return domainMapper.toDealDto(deal);
    }

    @Override
    @Transactional
    public void delete(Long creatorId, Long dealId) {
        brandDealDao.delete(requireDeal(creatorId, dealId));
    }

    private void applyRequest(BrandDeal deal, BrandDealRequest request) {
        deal.setBrandName(request.brandName());
        deal.setBrandLogo(blankToNull(request.brandLogo()));
        deal.setCategory(blankToNull(request.category()));
        deal.setContactPerson(blankToNull(request.contactPerson()));
        deal.setContactEmail(blankToNull(request.contactEmail()));
        deal.setContactPhone(blankToNull(request.contactPhone()));
        deal.setCampaignTitle(blankToNull(request.campaignTitle()));
        deal.setStartDate(request.startDate());
        deal.setEndDate(request.endDate());
        deal.setNegotiationNotes(blankToNull(request.negotiationNotes()));

        deal.setUsageRights(toUsageRights(request.usageRights()));

        deal.getTags().clear();
        if (request.tags() != null) {
            deal.getTags().addAll(new LinkedHashSet<>(request.tags().stream().filter(t -> t != null && !t.isBlank()).toList()));
        }

        deal.getDeliverables().clear();
        if (request.deliverables() != null) {
            int order = 0;
            for (DeliverableItemDto dto : request.deliverables()) {
                if (dto == null || dto.type() == null) {
                    continue;
                }
                deal.addDeliverable(DeliverableItem.builder().type(dto.type()).title(blankToNull(dto.title())).dueDate(dto.dueDate())
                        .status(dto.status() == null ? DeliverableStatus.PENDING : dto.status()).link(blankToNull(dto.link())).sortOrder(order++)
                        .build());
            }
        }
    }

    private UsageRights toUsageRights(UsageRightsDto dto) {
        if (dto == null) {
            return new UsageRights();
        }
        return UsageRights.builder().exclusivityDays(dto.exclusivityDays() == null ? 0 : dto.exclusivityDays())
                .paidAdsAllowed(Boolean.TRUE.equals(dto.paidAdsAllowed())).whitelistingAllowed(Boolean.TRUE.equals(dto.whitelistingAllowed()))
                .territory(blankToNull(dto.territory())).build();
    }

    private String nextDealNumber(Long creatorId) {
        long next = brandDealDao.countByCreatorId(creatorId) + 1;
        return "BD-%d-%02d".formatted(LocalDate.now().getYear(), next);
    }

    private BrandDeal requireDeal(Long creatorId, Long dealId) {
        return brandDealDao.findByIdAndCreatorId(dealId, creatorId).orElseThrow(() -> ResourceNotFoundException.of("Brand deal", dealId));
    }

    private Creator requireCreator(Long creatorId) {
        return creatorDao.findById(creatorId).orElseThrow(() -> ResourceNotFoundException.of("Creator", creatorId));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
