package com.creatoros.service;

import java.util.List;

import com.creatoros.dto.expense.ExpenseDto;
import com.creatoros.dto.expense.ExpenseRequest;

public interface ExpenseService {

    List<ExpenseDto> listForCreator(Long creatorId);

    ExpenseDto get(Long creatorId, Long expenseId);

    ExpenseDto create(Long creatorId, ExpenseRequest request);

    ExpenseDto update(Long creatorId, Long expenseId, ExpenseRequest request);

    void delete(Long creatorId, Long expenseId);
}
