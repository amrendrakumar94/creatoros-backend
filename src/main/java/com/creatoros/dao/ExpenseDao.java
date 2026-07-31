package com.creatoros.dao;

import java.util.List;
import java.util.Optional;

import com.creatoros.entity.Expense;

public interface ExpenseDao {

    Expense save(Expense expense);

    void delete(Expense expense);

    List<Expense> findByCreatorIdOrderByExpenseDateDescIdDesc(Long creatorId);

    Optional<Expense> findByIdAndCreatorId(Long id, Long creatorId);
}
