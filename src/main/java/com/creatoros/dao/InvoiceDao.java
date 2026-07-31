package com.creatoros.dao;

import com.creatoros.entity.Invoice;
import com.creatoros.entity.InvoiceStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvoiceDao {

    Invoice save(Invoice invoice);

    List<Invoice> saveAll(Collection<Invoice> invoices);

    /** Cascades to invoice items. */
    void delete(Invoice invoice);

    List<Invoice> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    /** Scoped lookup: an id belonging to another creator simply is not found. */
    Optional<Invoice> findByIdAndCreatorId(Long id, Long creatorId);

    long countByCreatorId(Long creatorId);

    List<Invoice> findByDealId(Long dealId);

    /** Issued invoices whose due date has passed - candidates to flip to Overdue. */
    List<Invoice> findByStatusInAndDueDateBefore(Collection<InvoiceStatus> statuses, LocalDate date);

    List<Invoice> findByCreatorIdAndStatusInAndDueDateBefore(
            Long creatorId, Collection<InvoiceStatus> statuses, LocalDate date);
}
