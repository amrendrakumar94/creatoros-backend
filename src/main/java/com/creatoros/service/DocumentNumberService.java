package com.creatoros.service;

import java.time.LocalDate;

public interface DocumentNumberService {

    String nextDealNumber(Long creatorId, LocalDate on);

    DocumentNumber nextInvoiceNumber(Long creatorId, LocalDate on);

    DocumentNumber nextQuotationNumber(Long creatorId, LocalDate on);
}
