package com.creatoros.serviceimpl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatoros.dao.BrandDealDao;
import com.creatoros.dao.CreatorDao;
import com.creatoros.dao.ExpenseDao;
import com.creatoros.dao.InvoiceDao;
import com.creatoros.dao.InvoicePaymentDao;
import com.creatoros.dto.dashboard.DashboardDto;
import com.creatoros.entity.Creator;
import com.creatoros.enums.PermissionKey;
import com.creatoros.exception.BadRequestException;
import com.creatoros.exception.ResourceNotFoundException;
import com.creatoros.service.DashboardService;
import com.creatoros.util.FinancialYear;
import com.creatoros.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CreatorDao         creatorDao;
    private final InvoiceDao         invoiceDao;
    private final InvoicePaymentDao  invoicePaymentDao;
    private final ExpenseDao         expenseDao;
    private final BrandDealDao       brandDealDao;
    private final DashboardAssembler dashboardAssembler;

    @Override
    @Transactional(readOnly = true)
    public DashboardDto summary(Long creatorId, String financialYear) {
        SecurityUtils.requireAny(PermissionKey.VIEW_DASHBOARD, PermissionKey.MANAGE_DEALS, PermissionKey.MANAGE_INVOICES,
                PermissionKey.MANAGE_EXPENSES, PermissionKey.MANAGE_FINANCES);
        Creator creator = creatorDao.findById(creatorId).orElseThrow(() -> ResourceNotFoundException.of("Creator", creatorId));

        LocalDate today = LocalDate.now();
        int startYear = resolveStartYear(financialYear, today);

        return dashboardAssembler.assemble(startYear, creator.isGstRegistered(), invoiceDao.findByCreatorIdOrderByIssueDateDescIdDesc(creatorId),
                invoicePaymentDao.findByCreatorIdOrderByReceivedOnAscIdAsc(creatorId),
                expenseDao.findByCreatorIdOrderByExpenseDateDescIdDesc(creatorId),
                brandDealDao.findByCreatorIdOrderByCreatedAtDesc(creatorId), today);
    }

    private int resolveStartYear(String financialYear, LocalDate today) {
        if (financialYear == null || financialYear.isBlank()) {
            return FinancialYear.startYearOf(today);
        }
        try {
            return FinancialYear.startYearOfLabel(financialYear.trim());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Financial year must look like 2026-27.", "INVALID_FINANCIAL_YEAR");
        }
    }
}
