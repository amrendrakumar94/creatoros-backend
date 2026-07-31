package com.creatoros.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.creatoros.service.InvoiceOverdueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
            log.error("Overdue invoice sweep failed", ex);
        }
    }
}
