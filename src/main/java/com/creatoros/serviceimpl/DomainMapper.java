package com.creatoros.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.creatoros.dto.deal.BrandDealDto;
import com.creatoros.dto.deal.DeliverableItemDto;
import com.creatoros.dto.deal.UsageRightsDto;
import com.creatoros.dto.expense.ExpenseDto;
import com.creatoros.entity.BrandDeal;
import com.creatoros.entity.DeliverableItem;
import com.creatoros.entity.Expense;
import com.creatoros.entity.UsageRights;

@Component
public class DomainMapper {

    private static String idOf(Long id) {
        return id == null ? null : String.valueOf(id);
    }

    // ---------- Brand deals ----------

    public BrandDealDto toDealDto(BrandDeal deal) {
        return new BrandDealDto(idOf(deal.getId()), deal.getDealNumber(), deal.getBrandName(), deal.getBrandLogo(), deal.getCategory(),
                deal.getContactPerson(), deal.getContactEmail(), deal.getContactPhone(), deal.getAmount(), deal.getStage(), deal.getPlatform(),
                deal.getCampaignTitle(), deal.getStartDate(), deal.getEndDate(),
                deal.getDeliverables() == null ? List.of() : deal.getDeliverables().stream().map(this::toDeliverableDto).toList(),
                toUsageRightsDto(deal.getUsageRights()), deal.getNegotiationNotes(), deal.getPaymentTerms(),
                deal.getTags() == null ? List.of() : new ArrayList<>(deal.getTags()));
    }

    private DeliverableItemDto toDeliverableDto(DeliverableItem item) {
        return new DeliverableItemDto(idOf(item.getId()), item.getType(), item.getTitle(), item.getDueDate(), item.getStatus(), item.getLink());
    }

    private UsageRightsDto toUsageRightsDto(UsageRights rights) {
        if (rights == null) {
            return new UsageRightsDto(0, false, false, null);
        }
        return new UsageRightsDto(rights.getExclusivityDays(), rights.isPaidAdsAllowed(), rights.isWhitelistingAllowed(), rights.getTerritory());
    }

    // ---------- Expenses ----------

    public ExpenseDto toExpenseDto(Expense expense) {
        return new ExpenseDto(idOf(expense.getId()), expense.getTitle(), expense.getCategory(), expense.getAmount(), expense.getExpenseDate(),
                expense.getVendor(), expense.getGstin(), expense.isHasGstInvoice(), expense.getGstClaimableAmount(), expense.getReceiptUrl(),
                expense.getPaymentMethod(), expense.getNotes(), expense.isTaxDeductible());
    }
}
