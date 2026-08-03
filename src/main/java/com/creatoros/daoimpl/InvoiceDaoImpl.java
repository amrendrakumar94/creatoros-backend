package com.creatoros.daoimpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.creatoros.dao.InvoiceDao;
import com.creatoros.entity.Invoice;
import com.creatoros.enums.InvoiceStatus;

@Repository
public class InvoiceDaoImpl extends HibernateDao implements InvoiceDao {

    @Override
    public Invoice save(Invoice invoice) {
        return persistOrMerge(invoice, invoice.getId());
    }

    @Override
    public List<Invoice> saveAll(Collection<Invoice> invoices) {
        List<Invoice> saved = new ArrayList<>(invoices.size());
        for (Invoice invoice : invoices) {
            saved.add(save(invoice));
        }
        return saved;
    }

    @Override
    public void delete(Invoice invoice) {
        removeEntity(invoice);
    }

    @Override
    public List<Invoice> findByCreatorIdOrderByCreatedAtDesc(Long creatorId) {
        return session().createSelectionQuery("from Invoice i where i.creator.id = :creatorId order by i.createdAt desc", Invoice.class)
                .setParameter("creatorId", creatorId).getResultList();
    }

    @Override
    public Optional<Invoice> findByIdAndCreatorId(Long id, Long creatorId) {
        return session().createSelectionQuery("from Invoice i where i.id = :id and i.creator.id = :creatorId", Invoice.class).setParameter("id", id)
                .setParameter("creatorId", creatorId).uniqueResultOptional();
    }

    @Override
    public long countByCreatorId(Long creatorId) {
        return session().createSelectionQuery("select count(i.id) from Invoice i where i.creator.id = :creatorId", Long.class)
                .setParameter("creatorId", creatorId).getSingleResult();
    }

    @Override
    public List<Invoice> findByDealId(Long dealId) {
        return session().createSelectionQuery("from Invoice i where i.dealId = :dealId", Invoice.class).setParameter("dealId", dealId)
                .getResultList();
    }

    @Override
    public List<Invoice> findByStatusInAndDueDateBefore(Collection<InvoiceStatus> statuses, LocalDate date) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        return session().createSelectionQuery("from Invoice i where i.status in :statuses and i.dueDate < :date", Invoice.class)
                .setParameterList("statuses", statuses).setParameter("date", date).getResultList();
    }

    @Override
    public List<Invoice> findByCreatorIdAndStatusInAndDueDateBefore(Long creatorId, Collection<InvoiceStatus> statuses, LocalDate date) {

        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        return session().createSelectionQuery("""
                from Invoice i
                 where i.creator.id = :creatorId
                   and i.status in :statuses
                   and i.dueDate < :date
                """, Invoice.class).setParameter("creatorId", creatorId).setParameterList("statuses", statuses).setParameter("date", date)
                .getResultList();
    }
}
