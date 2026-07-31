package com.creatoros.service;

import com.creatoros.dto.expense.ExpenseDto;
import com.creatoros.dto.expense.ExpenseRequest;

import java.util.List;

public interface ExpenseService {

    List<ExpenseDto> listForCreator(Long creatorId);

    ExpenseDto get(Long creatorId, Long expenseId);

    /** Input tax credit is computed server-side from the amount and GST-invoice flag. */
    ExpenseDto create(Long creatorId, ExpenseRequest request);

    ExpenseDto update(Long creatorId, Long expenseId, ExpenseRequest request);

    void delete(Long creatorId, Long expenseId);
}
