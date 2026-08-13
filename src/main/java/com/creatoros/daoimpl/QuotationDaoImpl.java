package com.creatoros.daoimpl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.creatoros.dao.QuotationDao;
import com.creatoros.entity.Quotation;

@Repository
public class QuotationDaoImpl extends HibernateDao implements QuotationDao {

    @Override
    public Quotation save(Quotation quotation) {
        return persistOrMerge(quotation, quotation.getId());
    }

    @Override
    public void delete(Quotation quotation) {
        removeEntity(quotation);
    }

    @Override
    public List<Quotation> findByCreatorIdOrderByIssueDateDescIdDesc(Long creatorId) {
        return session()
                .createSelectionQuery("from Quotation q where q.creator.id = :creatorId order by q.issueDate desc, q.id desc", Quotation.class)
                .setParameter("creatorId", creatorId).getResultList();
    }

    @Override
    public Optional<Quotation> findByIdAndCreatorId(Long id, Long creatorId) {
        return session().createSelectionQuery("from Quotation q where q.id = :id and q.creator.id = :creatorId", Quotation.class)
                .setParameter("id", id).setParameter("creatorId", creatorId).uniqueResultOptional();
    }

    @Override
    public List<Quotation> findDueScheduledSends(Timestamp now) {
        return session()
                .createSelectionQuery("from Quotation q where q.scheduledSendAt is not null and q.scheduledSendAt <= :now", Quotation.class)
                .setParameter("now", now).getResultList();
    }
}
