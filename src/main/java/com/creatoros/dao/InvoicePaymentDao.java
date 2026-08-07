package com.creatoros.dao;

import java.util.List;

import com.creatoros.entity.InvoicePayment;

public interface InvoicePaymentDao {

    InvoicePayment save(InvoicePayment payment);

    List<InvoicePayment> findByInvoiceIdOrderByReceivedOnAscIdAsc(Long invoiceId);
}
