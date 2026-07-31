package com.creatoros.repository;

import com.creatoros.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCreatorIdOrderByExpenseDateDescIdDesc(Long creatorId);

    Optional<Expense> findByIdAndCreatorId(Long id, Long creatorId);
}
