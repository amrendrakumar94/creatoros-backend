package com.creatoros.job;

import com.creatoros.service.InvoiceOverdueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Nightly sweep that flips past-due invoices to Overdue.
 *
 * <p>Runs shortly after midnight so an invoice is marked on the first morning it is actually
 * late. Creators who open the app between runs are covered separately: listing invoices
 * refreshes their own records first.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InvoiceOverdueJob {

    private final InvoiceOverdueService invoiceOverdueService;

    @Scheduled(cron = "${app.invoice.overdue-cron:0 15 0 * * *}", zone = "${app.invoice.timezone:Asia/Kolkata}")
    public void markOverdueInvoices() {
        try {
            int updated = invoiceOverdueService.refreshAll();
            if (updated > 0) {
                log.info("Overdue sweep updated {} invoice(s)", updated);
            }
        } catch (Exception ex) {
            // Never let a failed sweep kill the scheduler thread - it must run again tomorrow.
            log.error("Overdue invoice sweep failed", ex);
        }
    }
}
