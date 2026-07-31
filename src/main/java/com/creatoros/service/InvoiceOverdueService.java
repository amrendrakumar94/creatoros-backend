package com.creatoros.service;

public interface InvoiceOverdueService {
    int refreshForCreator(Long creatorId);

    int refreshAll();
}
