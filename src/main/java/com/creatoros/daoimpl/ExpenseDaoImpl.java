package com.creatoros.daoimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.creatoros.dao.ExpenseDao;
import com.creatoros.entity.Expense;

@Repository
public class ExpenseDaoImpl extends HibernateDao implements ExpenseDao {

    @Override
    public Expense save(Expense expense) {
        return persistOrMerge(expense, expense.getId());
    }

    @Override
    public void delete(Expense expense) {
        removeEntity(expense);
    }

    @Override
    public List<Expense> findByCreatorIdOrderByExpenseDateDescIdDesc(Long creatorId) {
        return session().createSelectionQuery("""
                from Expense e
                 where e.creator.id = :creatorId
                 order by e.expenseDate desc, e.id desc
                """, Expense.class).setParameter("creatorId", creatorId).getResultList();
    }

    @Override
    public Optional<Expense> findByIdAndCreatorId(Long id, Long creatorId) {
        return session().createSelectionQuery("from Expense e where e.id = :id and e.creator.id = :creatorId", Expense.class).setParameter("id", id)
                .setParameter("creatorId", creatorId).uniqueResultOptional();
    }
}
