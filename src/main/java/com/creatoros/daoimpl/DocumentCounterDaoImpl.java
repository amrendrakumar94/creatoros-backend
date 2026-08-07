package com.creatoros.daoimpl;

import org.springframework.stereotype.Repository;

import com.creatoros.dao.DocumentCounterDao;
import com.creatoros.enums.DocumentType;

@Repository
public class DocumentCounterDaoImpl extends HibernateDao implements DocumentCounterDao {

    private static final String UPSERT = """
            INSERT INTO document_counter (creator_id, doc_type, financial_year, last_sequence)
            VALUES (:creatorId, :docType, :financialYear, 1)
            ON DUPLICATE KEY UPDATE last_sequence = last_sequence + 1
            """;

    private static final String READ   = """
            SELECT last_sequence FROM document_counter
            WHERE creator_id = :creatorId AND doc_type = :docType AND financial_year = :financialYear
            """;

    @Override
    public int nextSequence(Long creatorId, DocumentType docType, String financialYear) {
        session().createNativeMutationQuery(UPSERT).setParameter("creatorId", creatorId).setParameter("docType", docType.name())
                .setParameter("financialYear", financialYear).executeUpdate();

        return session().createNativeQuery(READ, Integer.class).setParameter("creatorId", creatorId).setParameter("docType", docType.name())
                .setParameter("financialYear", financialYear).getSingleResult();
    }
}
