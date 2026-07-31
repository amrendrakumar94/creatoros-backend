package com.creatoros.daoimpl;

import com.creatoros.entity.Expense;
import com.creatoros.dao.ExpenseDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
        return session()
                .createSelectionQuery("""
                        from Expense e
                         where e.creator.id = :creatorId
                         order by e.expenseDate desc, e.id desc
                        """, Expense.class)
                .setParameter("creatorId", creatorId)
                .getResultList();
    }

    /** Scoped lookup: an id belonging to another creator simply is not found. */
    @Override
    public Optional<Expense> findByIdAndCreatorId(Long id, Long creatorId) {
        return session()
                .createSelectionQuery(
                        "from Expense e where e.id = :id and e.creator.id = :creatorId",
                        Expense.class)
                .setParameter("id", id)
                .setParameter("creatorId", creatorId)
                .uniqueResultOptional();
    }
}
