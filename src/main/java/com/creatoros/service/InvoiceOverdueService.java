package com.creatoros.service;

public interface InvoiceOverdueService {

    /**
     * Flips this creator's past-due invoices to Overdue.
     *
     * <p>Called before listing invoices so the UI is accurate the moment it loads, rather than
     * only after the nightly sweep has run.
     *
     * @return how many invoices changed status
     */
    int refreshForCreator(Long creatorId);

    /**
     * Sweeps every creator. Driven by the scheduled job so notifications go out on the day an
     * invoice becomes overdue, even for creators who are not currently using the app.
     *
     * @return how many invoices changed status
     */
    int refreshAll();
}
