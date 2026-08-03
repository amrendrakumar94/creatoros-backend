package com.creatoros.serviceimpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatoros.dao.InvoiceDao;
import com.creatoros.entity.Invoice;
import com.creatoros.enums.InvoiceStatus;
import com.creatoros.enums.NotificationType;
import com.creatoros.service.InvoiceOverdueService;
import com.creatoros.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceOverdueServiceImpl implements InvoiceOverdueService {

    private static final Set<InvoiceStatus> ELIGIBLE = Set.of(InvoiceStatus.SENT, InvoiceStatus.VIEWED);

    private final InvoiceDao                invoiceDao;
    private final NotificationService       notificationService;

    @Override
    @Transactional
    public int refreshForCreator(Long creatorId) {
        List<Invoice> due = invoiceDao.findByCreatorIdAndStatusInAndDueDateBefore(creatorId, ELIGIBLE, LocalDate.now());
        return markOverdue(due);
    }

    @Override
    @Transactional
    public int refreshAll() {
        List<Invoice> due = invoiceDao.findByStatusInAndDueDateBefore(ELIGIBLE, LocalDate.now());
        return markOverdue(due);
    }

    private int markOverdue(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return 0;
        }

        for (Invoice invoice : invoices) {
            invoice.setStatus(InvoiceStatus.OVERDUE);

            long daysLate = LocalDate.now().toEpochDay() - invoice.getDueDate().toEpochDay();
            notificationService.record(invoice.getCreator(), NotificationType.PAYMENT, "Invoice overdue: %s".formatted(invoice.getBrandName()),
                    "%s was due on %s and is now %d day%s late.".formatted(invoice.getInvoiceNumber(), invoice.getDueDate(), daysLate,
                            daysLate == 1 ? "" : "s"),
                    "payments", invoice.getNetReceivable());
        }

        invoiceDao.saveAll(invoices);
        log.info("Marked {} invoice(s) overdue", invoices.size());
        return invoices.size();
    }
}
