package com.creatoros.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.creatoros.entity.Quotation;

public interface QuotationDao {

    Quotation save(Quotation quotation);

    void delete(Quotation quotation);

    List<Quotation> findByCreatorIdOrderByIssueDateDescIdDesc(Long creatorId);

    Optional<Quotation> findByIdAndCreatorId(Long id, Long creatorId);

    /** Not creator-scoped - backs the scheduled-send poller, which runs across every tenant. */
    List<Quotation> findDueScheduledSends(Timestamp now);
}
