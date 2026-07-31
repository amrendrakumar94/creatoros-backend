package com.creatoros.daoimpl;

import com.creatoros.entity.Creator;
import com.creatoros.entity.OtpPurpose;
import com.creatoros.entity.OtpToken;
import com.creatoros.dao.OtpTokenDao;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class OtpTokenDaoImpl extends HibernateDao implements OtpTokenDao {

    @Override
    public OtpToken save(OtpToken token) {
        return persistOrMerge(token, token.getId());
    }

    /**
     * The newest still-redeemable code for this creator and purpose.
     */
    @Override
    public Optional<OtpToken> findFirstByCreatorAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(Creator creator, OtpPurpose purpose) {

        return session().createSelectionQuery("""
                from OtpToken o
                 where o.creator = :creator
                   and o.purpose = :purpose
                   and o.consumedAt is null
                 order by o.createdAt desc
                """, OtpToken.class).setParameter("creator", creator).setParameter("purpose", purpose).setMaxResults(1).getResultList().stream().findFirst();
    }

    /**
     * Burns every outstanding code for a creator/purpose before a new one is
     * issued, so only the most recently sent code is ever valid.
     */
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
