package com.creatoros.serviceimpl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatoros.dao.CreatorDao;
import com.creatoros.dao.ExpenseDao;
import com.creatoros.dto.expense.ExpenseDto;
import com.creatoros.dto.expense.ExpenseRequest;
import com.creatoros.entity.Creator;
import com.creatoros.entity.Expense;
import com.creatoros.exception.ResourceNotFoundException;
import com.creatoros.service.ExpenseService;
import com.creatoros.service.GstCalculationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseDao            expenseDao;
    private final CreatorDao            creatorDao;
    private final GstCalculationService gstCalculationService;
    private final DomainMapper          domainMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDto> listForCreator(Long creatorId) {
        return expenseDao.findByCreatorIdOrderByExpenseDateDescIdDesc(creatorId).stream().map(domainMapper::toExpenseDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDto get(Long creatorId, Long expenseId) {
        return domainMapper.toExpenseDto(requireExpense(creatorId, expenseId));
    }

    @Override
    @Transactional
    public ExpenseDto create(Long creatorId, ExpenseRequest request) {
        Creator creator = creatorDao.findById(creatorId).orElseThrow(() -> ResourceNotFoundException.of("Creator", creatorId));

        Expense expense = Expense.builder().creator(creator).build();
        applyRequest(expense, request);

        expenseDao.save(expense);
        log.debug("Recorded expense {} for creator {}", expense.getId(), creatorId);

        return domainMapper.toExpenseDto(expense);
    }

    @Override
    @Transactional
    public ExpenseDto update(Long creatorId, Long expenseId, ExpenseRequest request) {
        Expense expense = requireExpense(creatorId, expenseId);
        applyRequest(expense, request);
        return domainMapper.toExpenseDto(expenseDao.save(expense));
    }

    @Override
    @Transactional
    public void delete(Long creatorId, Long expenseId) {
        expenseDao.delete(requireExpense(creatorId, expenseId));
    }

    private void applyRequest(Expense expense, ExpenseRequest request) {
        expense.setTitle(request.title());
        expense.setCategory(request.category());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.date() == null ? LocalDate.now() : request.date());
        expense.setVendor(blankToNull(request.vendor()));
        expense.setPaymentMethod(request.paymentMethod());
        expense.setNotes(blankToNull(request.notes()));
        expense.setReceiptUrl(blankToNull(request.receiptUrl()));
        expense.setTaxDeductible(request.taxDeductible() == null || request.taxDeductible());

        boolean hasGstInvoice = request.hasGstInvoice();
        expense.setHasGstInvoice(hasGstInvoice);
        expense.setGstin(hasGstInvoice ? upperOrNull(request.gstin()) : null);
        expense.setGstClaimableAmount(gstCalculationService.calculateInputTaxCredit(request.amount(), hasGstInvoice));
    }

    private Expense requireExpense(Long creatorId, Long expenseId) {
        return expenseDao.findByIdAndCreatorId(expenseId, creatorId).orElseThrow(() -> ResourceNotFoundException.of("Expense", expenseId));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String upperOrNull(String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }
}
