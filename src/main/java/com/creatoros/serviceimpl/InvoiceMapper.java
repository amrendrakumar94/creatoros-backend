package com.creatoros.serviceimpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.creatoros.dao.InvoicePaymentDao;
import com.creatoros.dto.invoice.InvoiceDto;
import com.creatoros.dto.invoice.InvoiceLineItemDto;
import com.creatoros.dto.invoice.InvoicePartyDto;
import com.creatoros.dto.invoice.InvoicePaymentDto;
import com.creatoros.entity.BuyerSnapshot;
import com.creatoros.entity.Invoice;
import com.creatoros.entity.InvoiceLineItem;
import com.creatoros.entity.InvoicePayment;
import com.creatoros.entity.SupplierSnapshot;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InvoiceMapper {

    private final InvoicePaymentDao invoicePaymentDao;

    public InvoiceDto toDto(Invoice invoice) {
        LocalDate today = LocalDate.now();
        List<InvoicePayment> payments = invoicePaymentDao.findByInvoiceIdOrderByReceivedOnAscIdAsc(invoice.getId());

        BigDecimal withheld = payments.stream().map(InvoicePayment::getTdsWithheld).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxableValue = invoice.getSubtotal().subtract(invoice.getDiscountAmount());

        return new InvoiceDto(idOf(invoice.getId()), invoice.getInvoiceNumber(), invoice.getFinancialYear(),
                invoice.getDeal() == null ? null : idOf(invoice.getDeal().getId()),
                invoice.getDeal() == null ? null : invoice.getDeal().getDealNumber(), invoice.getStatus(), invoice.isOverdue(today),
                invoice.daysOverdue(today), invoice.getIssueDate(), invoice.getDueDate(), invoice.getPaymentTerms(),
                toSupplierParty(invoice.getSupplier()), toBuyerParty(invoice.getBuyer()), invoice.getPlaceOfSupplyState(),
                invoice.getPlaceOfSupplyCode(), invoice.isInterState(), invoice.isReverseCharge(), invoice.getTotalTax().signum() > 0,
                invoice.getSubtotal(), invoice.getDiscountAmount(), taxableValue, invoice.getCgstRate(), invoice.getCgstAmount(),
                invoice.getSgstRate(), invoice.getSgstAmount(), invoice.getIgstRate(), invoice.getIgstAmount(), invoice.getTotalTax(),
                invoice.getInvoiceTotal(), invoice.getTdsSection(), invoice.getTdsRate(), invoice.getTdsAmount(), invoice.getNetReceivable(),
                invoice.getAmountPaid(), withheld, invoice.getBalanceDue(), invoice.getNotes(), invoice.getTerms(),
                invoice.getLineItems().stream().map(this::toLineItemDto).toList(), payments.stream().map(this::toPaymentDto).toList(),
                invoice.getScheduledSendAt(), invoice.getScheduledSendEmail(), invoice.getLastEmailedAt(), invoice.getRazorpayPaymentLinkUrl());
    }

    private InvoiceLineItemDto toLineItemDto(InvoiceLineItem item) {
        return new InvoiceLineItemDto(idOf(item.getId()), item.getDescription(), item.getSacCode(), item.getQuantity(), item.getUnit(),
                item.getRate(), item.getGstRate(), item.getTaxableAmount());
    }

    private InvoicePaymentDto toPaymentDto(InvoicePayment payment) {
        return new InvoicePaymentDto(idOf(payment.getId()), payment.getAmount(), payment.getReceivedOn(), payment.getMethod(),
                payment.getReference(), payment.getTdsWithheld(), payment.getNotes());
    }

    private InvoicePartyDto toSupplierParty(SupplierSnapshot supplier) {
        if (supplier == null) {
            return null;
        }
        return new InvoicePartyDto(supplier.getLegalName(), supplier.getLegalName(), supplier.getGstin(), supplier.getPan(), null,
                supplier.getAddress(), supplier.getCity(), supplier.getState(), supplier.getStateCode(), supplier.getPincode());
    }

    private InvoicePartyDto toBuyerParty(BuyerSnapshot buyer) {
        if (buyer == null) {
            return null;
        }
        return new InvoicePartyDto(buyer.getName(), buyer.getLegalName(), buyer.getGstin(), null, buyer.getEmail(), buyer.getAddress(),
                buyer.getCity(), buyer.getState(), buyer.getStateCode(), buyer.getPincode());
    }

    private static String idOf(Long id) {
        return id == null ? null : String.valueOf(id);
    }
}
