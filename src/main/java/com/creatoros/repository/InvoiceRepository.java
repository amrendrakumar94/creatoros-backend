package com.creatoros.repository;

import com.creatoros.entity.Invoice;
import com.creatoros.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    Optional<Invoice> findByIdAndCreatorId(Long id, Long creatorId);

    long countByCreatorId(Long creatorId);

    List<Invoice> findByDealId(Long dealId);

    /** Issued invoices whose due date has passed - candidates to flip to Overdue. */
    List<Invoice> findByStatusInAndDueDateBefore(Collection<InvoiceStatus> statuses, LocalDate date);

    List<Invoice> findByCreatorIdAndStatusInAndDueDateBefore(
            Long creatorId, Collection<InvoiceStatus> statuses, LocalDate date);
}
