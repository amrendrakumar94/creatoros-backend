package com.creatoros.dao;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.creatoros.entity.Invoice;
import com.creatoros.enums.InvoiceStatus;

public interface InvoiceDao {

    Invoice save(Invoice invoice);

    List<Invoice> saveAll(Collection<Invoice> invoices);

    void delete(Invoice invoice);

    List<Invoice> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    Optional<Invoice> findByIdAndCreatorId(Long id, Long creatorId);

    long countByCreatorId(Long creatorId);

    List<Invoice> findByDealId(Long dealId);

    List<Invoice> findByStatusInAndDueDateBefore(Collection<InvoiceStatus> statuses, LocalDate date);

    List<Invoice> findByCreatorIdAndStatusInAndDueDateBefore(Long creatorId, Collection<InvoiceStatus> statuses, LocalDate date);
}
