package com.creatoros.service;

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
}
