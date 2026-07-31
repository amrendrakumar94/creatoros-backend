package com.creatoros.service;

import com.creatoros.dto.invoice.CreateInvoiceRequest;
import com.creatoros.dto.invoice.InvoiceDto;
import com.creatoros.dto.invoice.SendReminderRequest;
import com.creatoros.dto.invoice.UpdateInvoiceStatusRequest;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceService {

    List<InvoiceDto> listForCreator(Long creatorId);

    InvoiceDto get(Long creatorId, Long invoiceId);

    /** Assigns COS-YYYY-NNN and computes every GST/TDS figure server-side. */
    InvoiceDto create(Long creatorId, CreateInvoiceRequest request);

    InvoiceDto updateStatus(Long creatorId, Long invoiceId, UpdateInvoiceStatusRequest request);

    InvoiceDto markPaid(Long creatorId, Long invoiceId, LocalDate paidDate);

    InvoiceDto sendReminder(Long creatorId, Long invoiceId, SendReminderRequest request);

    InvoiceDto setExpectedSettlementDate(Long creatorId, Long invoiceId, LocalDate expectedDate);

    void delete(Long creatorId, Long invoiceId);
}
