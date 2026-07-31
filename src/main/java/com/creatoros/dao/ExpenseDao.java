package com.creatoros.dao;

import com.creatoros.entity.Expense;

import java.util.List;
import java.util.Optional;

public interface ExpenseDao {

    Expense save(Expense expense);

    void delete(Expense expense);

    List<Expense> findByCreatorIdOrderByExpenseDateDescIdDesc(Long creatorId);

    /** Scoped lookup: an id belonging to another creator simply is not found. */
    Optional<Expense> findByIdAndCreatorId(Long id, Long creatorId);
}
