package com.creatoros.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.creatoros.dto.invoice.InvoiceDto;
import com.creatoros.dto.invoice.InvoiceRequest;
import com.creatoros.dto.invoice.RecordPaymentRequest;
import com.creatoros.enums.InvoiceStatus;

public interface InvoiceService {

    List<InvoiceDto> listForCreator(Long creatorId);

    InvoiceDto get(Long creatorId, Long invoiceId);

    InvoiceDto create(Long creatorId, InvoiceRequest request);

    InvoiceDto update(Long creatorId, Long invoiceId, InvoiceRequest request);

    InvoiceDto updateStatus(Long creatorId, Long invoiceId, InvoiceStatus status);

    InvoiceDto recordPayment(Long creatorId, Long invoiceId, RecordPaymentRequest request);

    void delete(Long creatorId, Long invoiceId);

    InvoiceDto sendNow(Long creatorId, Long invoiceId, String toEmail);

    InvoiceDto scheduleSend(Long creatorId, Long invoiceId, String toEmail, LocalDateTime sendAt);

    InvoiceDto cancelScheduledSend(Long creatorId, Long invoiceId);

    String getInvoiceHtml(Long creatorId, Long invoiceId);

    byte[] getInvoicePdf(Long creatorId, Long invoiceId);

    /** Creates (or refreshes) a Razorpay payment link for the invoice's current balance due. */
    InvoiceDto createPaymentLink(Long creatorId, Long invoiceId);

    /**
     * Records a Razorpay-confirmed payment. Deliberately not scoped to a creator - the webhook
     * that calls this has no acting tenant, only an invoice id; its own signature check is what
     * stands in for authentication here, the same way {@code findDueScheduledSends} is unscoped
     * because the scheduler has no acting tenant either.
     */
    void recordGatewayPayment(Long invoiceId, BigDecimal amount, String razorpayPaymentId);
}
