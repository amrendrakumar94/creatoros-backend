package com.creatoros.service;

import java.time.LocalDateTime;
import java.util.List;

import com.creatoros.dto.quotation.QuotationDto;
import com.creatoros.dto.quotation.QuotationRequest;
import com.creatoros.enums.QuotationStatus;

public interface QuotationService {

    List<QuotationDto> listForCreator(Long creatorId);

    QuotationDto get(Long creatorId, Long quotationId);

    QuotationDto create(Long creatorId, QuotationRequest request);

    QuotationDto update(Long creatorId, Long quotationId, QuotationRequest request);

    QuotationDto updateStatus(Long creatorId, Long quotationId, QuotationStatus status);

    void delete(Long creatorId, Long quotationId);

    QuotationDto sendNow(Long creatorId, Long quotationId, String toEmail);

    QuotationDto scheduleSend(Long creatorId, Long quotationId, String toEmail, LocalDateTime sendAt);

    QuotationDto cancelScheduledSend(Long creatorId, Long quotationId);

    String getQuotationHtml(Long creatorId, Long quotationId);

    byte[] getQuotationPdf(Long creatorId, Long quotationId);

    QuotationDto convertToInvoice(Long creatorId, Long quotationId);
}
