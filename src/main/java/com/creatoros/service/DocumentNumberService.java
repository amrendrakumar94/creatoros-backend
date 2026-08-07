package com.creatoros.service;

import java.time.LocalDate;

public interface DocumentNumberService {

    String nextDealNumber(Long creatorId, LocalDate on);

    String nextInvoiceNumber(Long creatorId, LocalDate on);
}
