package com.creatoros.daoimpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.creatoros.dao.TeamInvitationDao;
import com.creatoros.entity.TeamInvitation;

@Repository
public class TeamInvitationDaoImpl extends HibernateDao implements TeamInvitationDao {

    @Override
    public TeamInvitation save(TeamInvitation invitation) {
        return persistOrMerge(invitation, invitation.getId());
    }

    @Override
    public Optional<TeamInvitation> findByInviteToken(String inviteToken) {
        return session().createSelectionQuery("from TeamInvitation i where i.inviteToken = :token", TeamInvitation.class)
                .setParameter("token", inviteToken).uniqueResultOptional();
    }

    @Override
    public Optional<TeamInvitation> findByCreatorIdAndEmailIgnoreCaseAndRevokedFalse(Long creatorId, String email) {
        if (email == null) {
            return Optional.empty();
        }
        return session().createSelectionQuery(
                "from TeamInvitation i where i.creatorId = :creatorId and lower(i.email) = :email and i.revoked = false and i.acceptedAt is null",
                TeamInvitation.class).setParameter("creatorId", creatorId).setParameter("email", email.toLowerCase(Locale.ROOT))
                .uniqueResultOptional();
    }

    @Override
    public Optional<TeamInvitation> findFirstByEmailIgnoreCaseAndRevokedFalseAndAcceptedAtIsNull(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return session().createSelectionQuery(
                "from TeamInvitation i where lower(i.email) = :email and i.revoked = false and i.acceptedAt is null order by i.createdAt desc",
                TeamInvitation.class).setParameter("email", email.toLowerCase(Locale.ROOT)).setMaxResults(1).uniqueResultOptional();
    }

    @Override
    public Optional<TeamInvitation> findByIdAndCreatorId(Long id, Long creatorId) {
        return session().createSelectionQuery("from TeamInvitation i where i.id = :id and i.creatorId = :creatorId", TeamInvitation.class)
                .setParameter("id", id).setParameter("creatorId", creatorId).uniqueResultOptional();
    }

    @Override
    public List<TeamInvitation> findPendingByCreatorIdOrderByCreatedAtDesc(Long creatorId) {
        return session().createSelectionQuery(
                "from TeamInvitation i where i.creatorId = :creatorId and i.revoked = false and i.acceptedAt is null "
                        + "and i.expiresOn >= :today order by i.createdAt desc",
                TeamInvitation.class).setParameter("creatorId", creatorId).setParameter("today", LocalDate.now()).getResultList();
    }
}
