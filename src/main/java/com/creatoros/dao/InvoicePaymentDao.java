package com.creatoros.dao;

import java.util.List;
import java.util.Optional;

import com.creatoros.entity.InvoicePayment;

public interface InvoicePaymentDao {

    InvoicePayment save(InvoicePayment payment);

    List<InvoicePayment> findByInvoiceIdOrderByReceivedOnAscIdAsc(Long invoiceId);

    List<InvoicePayment> findByCreatorIdOrderByReceivedOnAscIdAsc(Long creatorId);

    /** Backs webhook idempotency - a re-delivered Razorpay event must not record a payment twice. */
    Optional<InvoicePayment> findByRazorpayPaymentId(String razorpayPaymentId);
}
