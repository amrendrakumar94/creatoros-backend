package com.creatoros.service;

import com.creatoros.entity.Invoice;
import com.creatoros.entity.InvoiceStatus;
import com.creatoros.entity.NotificationType;
import com.creatoros.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Moves issued invoices to Overdue once their due date has passed.
 *
 * <p>Without this, {@code Overdue} was unreachable: nothing ever set it, so the dashboard's
 * overdue card, the sidebar badge and the payment-collection screen all read as empty no matter
 * how late a brand was.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceOverdueServiceImpl implements InvoiceOverdueService {

    /**
     * Only invoices that have actually been issued can fall overdue. A Draft was never sent, and
     * Paid is settled.
     *
     * <p>Partially Paid is deliberately excluded for now - a part-settled invoice past its due
     * date is arguably overdue too, but flipping it would change what the creator sees about
     * money they have already partly received, so that call is left open.
     */
    private static final Set<InvoiceStatus> ELIGIBLE =
            Set.of(InvoiceStatus.SENT, InvoiceStatus.VIEWED);

    private final InvoiceRepository invoiceRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public int refreshForCreator(Long creatorId) {
        List<Invoice> due = invoiceRepository.findByCreatorIdAndStatusInAndDueDateBefore(
                creatorId, ELIGIBLE, LocalDate.now());
        return markOverdue(due);
    }

    @Override
    @Transactional
    public int refreshAll() {
        List<Invoice> due = invoiceRepository.findByStatusInAndDueDateBefore(ELIGIBLE, LocalDate.now());
        return markOverdue(due);
    }

    private int markOverdue(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return 0;
        }

        for (Invoice invoice : invoices) {
            invoice.setStatus(InvoiceStatus.OVERDUE);

            long daysLate = LocalDate.now().toEpochDay() - invoice.getDueDate().toEpochDay();
            notificationService.record(
                    invoice.getCreator(),
                    NotificationType.PAYMENT,
                    "Invoice overdue: %s".formatted(invoice.getBrandName()),
                    "%s was due on %s and is now %d day%s late.".formatted(
                            invoice.getInvoiceNumber(),
                            invoice.getDueDate(),
                            daysLate,
                            daysLate == 1 ? "" : "s"),
                    "payments",
                    invoice.getNetReceivable());
        }

        invoiceRepository.saveAll(invoices);
        log.info("Marked {} invoice(s) overdue", invoices.size());
        return invoices.size();
    }
}
