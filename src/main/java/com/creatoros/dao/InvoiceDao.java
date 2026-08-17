package com.creatoros.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.creatoros.entity.Invoice;

public interface InvoiceDao {

    Invoice save(Invoice invoice);

    void delete(Invoice invoice);

    List<Invoice> findByCreatorIdOrderByIssueDateDescIdDesc(Long creatorId);

    Optional<Invoice> findByIdAndCreatorId(Long id, Long creatorId);

    /** Not creator-scoped - backs the scheduled-send poller, which runs across every tenant. */
    List<Invoice> findDueScheduledSends(Timestamp now);

    /** Not creator-scoped - backs the Razorpay webhook, which only has an invoice id, no acting tenant. */
    Optional<Invoice> findById(Long id);
}
