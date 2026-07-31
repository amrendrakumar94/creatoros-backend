package com.creatoros.daoimpl;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.creatoros.dao.OtpTokenDao;
import com.creatoros.entity.Creator;
import com.creatoros.entity.OtpPurpose;
import com.creatoros.entity.OtpToken;

@Repository
public class OtpTokenDaoImpl extends HibernateDao implements OtpTokenDao {

    @Override
    public OtpToken save(OtpToken token) {
        return persistOrMerge(token, token.getId());
    }

    @Override
    public Optional<OtpToken> findFirstByCreatorAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(Creator creator, OtpPurpose purpose) {

        return session().createSelectionQuery("""
                from OtpToken o
                 where o.creator = :creator
                   and o.purpose = :purpose
                   and o.consumedAt is null
                 order by o.createdAt desc
                """, OtpToken.class).setParameter("creator", creator).setParameter("purpose", purpose).setMaxResults(1).getResultList().stream()
                .findFirst();
    }

    @Override
    public int consumeOutstanding(Creator creator, OtpPurpose purpose, Instant now) {
        return executeBulk(session().createMutationQuery("""
                update OtpToken o
                   set o.consumedAt = :now
                 where o.creator = :creator
                   and o.purpose = :purpose
                   and o.consumedAt is null
                """).setParameter("creator", creator).setParameter("purpose", purpose).setParameter("now", now));
    }
}
