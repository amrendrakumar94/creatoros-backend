package com.creatoros.serviceimpl;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.creatoros.dao.InvoiceDao;
import com.creatoros.dto.quotation.QuotationDto;
import com.creatoros.dto.quotation.QuotationLineItemDto;
import com.creatoros.dto.quotation.QuotationPartyDto;
import com.creatoros.entity.BuyerSnapshot;
import com.creatoros.entity.Quotation;
import com.creatoros.entity.QuotationLineItem;
import com.creatoros.entity.SupplierSnapshot;
import com.creatoros.enums.QuotationStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuotationMapper {

    private final InvoiceDao invoiceDao;

    public QuotationDto toDto(Quotation quotation) {
        boolean expired = quotation.getStatus() == QuotationStatus.EXPIRED;
        BigDecimal taxableValue = quotation.getSubtotal().subtract(quotation.getDiscountAmount());

        String convertedInvoiceId = quotation.getConvertedInvoiceId() == null ? null : idOf(quotation.getConvertedInvoiceId());
        String convertedInvoiceNumber = quotation.getConvertedInvoiceId() == null ? null
                : invoiceDao.findByIdAndCreatorId(quotation.getConvertedInvoiceId(), quotation.getCreator().getId())
                        .map(invoice -> invoice.getInvoiceNumber()).orElse(null);

        return new QuotationDto(idOf(quotation.getId()), quotation.getQuotationNumber(), quotation.getFinancialYear(),
                quotation.getDeal() == null ? null : idOf(quotation.getDeal().getId()),
                quotation.getDeal() == null ? null : quotation.getDeal().getDealNumber(), quotation.getStatus(), quotation.getIssueDate(),
                quotation.getValidUntil(), expired, toSupplierParty(quotation.getSupplier()), toBuyerParty(quotation.getBuyer()),
                quotation.getPlaceOfSupplyState(), quotation.getPlaceOfSupplyCode(), quotation.isInterState(), quotation.isReverseCharge(),
                quotation.getTotalTax().signum() > 0, quotation.getSubtotal(), quotation.getDiscountAmount(), taxableValue,
                quotation.getCgstRate(), quotation.getCgstAmount(), quotation.getSgstRate(), quotation.getSgstAmount(), quotation.getIgstRate(),
                quotation.getIgstAmount(), quotation.getTotalTax(), quotation.getQuotationTotal(), quotation.getTdsSection(),
                quotation.getTdsRate(), quotation.getTdsAmount(), quotation.getNotes(), quotation.getTerms(),
                quotation.getLineItems().stream().map(this::toLineItemDto).toList(), quotation.getScheduledSendAt(),
                quotation.getScheduledSendEmail(), quotation.getLastEmailedAt(), convertedInvoiceId, convertedInvoiceNumber);
    }

    private QuotationLineItemDto toLineItemDto(QuotationLineItem item) {
        return new QuotationLineItemDto(idOf(item.getId()), item.getDescription(), item.getSacCode(), item.getQuantity(), item.getUnit(),
                item.getRate(), item.getGstRate(), item.getTaxableAmount());
    }

    private QuotationPartyDto toSupplierParty(SupplierSnapshot supplier) {
        if (supplier == null) {
            return null;
        }
        return new QuotationPartyDto(supplier.getLegalName(), supplier.getLegalName(), supplier.getGstin(), supplier.getPan(), null,
                supplier.getAddress(), supplier.getCity(), supplier.getState(), supplier.getStateCode(), supplier.getPincode());
    }

    private QuotationPartyDto toBuyerParty(BuyerSnapshot buyer) {
        if (buyer == null) {
            return null;
        }
        return new QuotationPartyDto(buyer.getName(), buyer.getLegalName(), buyer.getGstin(), null, buyer.getEmail(), buyer.getAddress(),
                buyer.getCity(), buyer.getState(), buyer.getStateCode(), buyer.getPincode());
    }

    private static String idOf(Long id) {
        return id == null ? null : String.valueOf(id);
    }
}
