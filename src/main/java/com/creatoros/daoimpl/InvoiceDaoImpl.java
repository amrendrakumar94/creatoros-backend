package com.creatoros.daoimpl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.creatoros.dao.InvoiceDao;
import com.creatoros.entity.Invoice;

@Repository
public class InvoiceDaoImpl extends HibernateDao implements InvoiceDao {

    @Override
    public Invoice save(Invoice invoice) {
        return persistOrMerge(invoice, invoice.getId());
    }

    @Override
    public void delete(Invoice invoice) {
        removeEntity(invoice);
    }

    @Override
    public List<Invoice> findByCreatorIdOrderByIssueDateDescIdDesc(Long creatorId) {
        return session()
                .createSelectionQuery("from Invoice i where i.creator.id = :creatorId order by i.issueDate desc, i.id desc", Invoice.class)
                .setParameter("creatorId", creatorId).getResultList();
    }

    @Override
    public Optional<Invoice> findByIdAndCreatorId(Long id, Long creatorId) {
        return session().createSelectionQuery("from Invoice i where i.id = :id and i.creator.id = :creatorId", Invoice.class)
                .setParameter("id", id).setParameter("creatorId", creatorId).uniqueResultOptional();
    }

    @Override
    public List<Invoice> findDueScheduledSends(Timestamp now) {
        return session().createSelectionQuery("from Invoice i where i.scheduledSendAt is not null and i.scheduledSendAt <= :now", Invoice.class)
                .setParameter("now", now).getResultList();
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return session().createSelectionQuery("from Invoice i where i.id = :id", Invoice.class).setParameter("id", id).uniqueResultOptional();
    }
}
