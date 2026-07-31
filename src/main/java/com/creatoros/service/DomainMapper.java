package com.creatoros.service;

import com.creatoros.dto.deal.BrandDealDto;
import com.creatoros.dto.deal.DeliverableItemDto;
import com.creatoros.dto.deal.UsageRightsDto;
import com.creatoros.dto.expense.ExpenseDto;
import com.creatoros.dto.invoice.InvoiceBankDto;
import com.creatoros.dto.invoice.InvoiceDto;
import com.creatoros.dto.invoice.InvoiceItemDto;
import com.creatoros.dto.notification.NotificationDto;
import com.creatoros.entity.BrandDeal;
import com.creatoros.entity.DeliverableItem;
import com.creatoros.entity.Expense;
import com.creatoros.entity.Invoice;
import com.creatoros.entity.InvoiceBankSnapshot;
import com.creatoros.entity.InvoiceItem;
import com.creatoros.entity.Notification;
import com.creatoros.entity.UsageRights;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity to DTO conversion for the operating domain.
 *
 * <p>Numeric ids are rendered as strings because the frontend types model every id as a string.
 */
@Component
public class DomainMapper {

    private static String idOf(Long id) {
        return id == null ? null : String.valueOf(id);
    }

    // ---------- Brand deals ----------

    public BrandDealDto toDealDto(BrandDeal deal) {
        return new BrandDealDto(
                idOf(deal.getId()),
                deal.getDealNumber(),
                deal.getBrandName(),
                deal.getBrandLogo(),
                deal.getCategory(),
                deal.getContactPerson(),
                deal.getContactEmail(),
                deal.getContactPhone(),
                deal.getAmount(),
                deal.getStage(),
                deal.getPlatform(),
                deal.getCampaignTitle(),
                deal.getStartDate(),
                deal.getEndDate(),
                deal.getDeliverables() == null
                        ? List.of()
                        : deal.getDeliverables().stream().map(this::toDeliverableDto).toList(),
                toUsageRightsDto(deal.getUsageRights()),
                deal.getNegotiationNotes(),
                deal.getPaymentTerms(),
                idOf(deal.getInvoiceId()),
                deal.getTags() == null ? List.of() : new ArrayList<>(deal.getTags()));
    }

    private DeliverableItemDto toDeliverableDto(DeliverableItem item) {
        return new DeliverableItemDto(
                idOf(item.getId()),
                item.getType(),
                item.getTitle(),
                item.getDueDate(),
                item.getStatus(),
                item.getLink());
    }

    private UsageRightsDto toUsageRightsDto(UsageRights rights) {
        if (rights == null) {
            return new UsageRightsDto(0, false, false, null);
        }
        return new UsageRightsDto(
                rights.getExclusivityDays(),
                rights.isPaidAdsAllowed(),
                rights.isWhitelistingAllowed(),
                rights.getTerritory());
    }

    // ---------- Invoices ----------

    public InvoiceDto toInvoiceDto(Invoice invoice) {
        return new InvoiceDto(
                idOf(invoice.getId()),
                invoice.getInvoiceNumber(),
                invoice.getBrandName(),
                invoice.getBrandGstin(),
                invoice.getBrandAddress(),
                invoice.getCreatorName(),
                invoice.getCreatorGstin(),
                invoice.getCreatorPan(),
                toInvoiceBankDto(invoice.getBankDetails()),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getItems() == null
                        ? List.of()
                        : invoice.getItems().stream().map(this::toInvoiceItemDto).toList(),
                invoice.getSubtotal(),
                invoice.isInterstate(),
                invoice.getCgstAmount(),
                invoice.getSgstAmount(),
                invoice.getIgstAmount(),
                invoice.getTotalGst(),
                invoice.getTdsDeducted(),
                invoice.getTotalAmount(),
                invoice.getNetReceivable(),
                invoice.getStatus(),
                idOf(invoice.getDealId()),
                invoice.getPaidDate(),
                invoice.getReminderSentCount(),
                invoice.getLastReminderDate(),
                invoice.getExpectedSettlementDate());
    }

    private InvoiceItemDto toInvoiceItemDto(InvoiceItem item) {
        return new InvoiceItemDto(
                idOf(item.getId()),
                item.getDescription(),
                item.getSacCode(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getAmount());
    }

    private InvoiceBankDto toInvoiceBankDto(InvoiceBankSnapshot bank) {
        if (bank == null) {
            return new InvoiceBankDto(null, null, null, null);
        }
        return new InvoiceBankDto(
                bank.getBankName(),
                bank.getAccountNumber(),
                bank.getIfscCode(),
                bank.getUpiId());
    }

    // ---------- Expenses ----------

    public ExpenseDto toExpenseDto(Expense expense) {
        return new ExpenseDto(
                idOf(expense.getId()),
                expense.getTitle(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getVendor(),
                expense.getGstin(),
                expense.isHasGstInvoice(),
                expense.getGstClaimableAmount(),
                expense.getReceiptUrl(),
                expense.getPaymentMethod(),
                expense.getNotes(),
                expense.isTaxDeductible());
    }

    // ---------- Notifications ----------

    public NotificationDto toNotificationDto(Notification notification) {
        return new NotificationDto(
                idOf(notification.getId()),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getType(),
                notification.isRead(),
                notification.getActionUrl(),
                notification.getAmount());
    }
}
