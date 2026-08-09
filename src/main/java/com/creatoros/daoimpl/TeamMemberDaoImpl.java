package com.creatoros.daoimpl;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.creatoros.dao.TeamMemberDao;
import com.creatoros.entity.TeamMember;

@Repository
public class TeamMemberDaoImpl extends HibernateDao implements TeamMemberDao {

    @Override
    public TeamMember save(TeamMember member) {
        return persistOrMerge(member, member.getId());
    }

    @Override
    public Optional<TeamMember> findByIdAndCreatorId(Long id, Long creatorId) {
        return session().createSelectionQuery("from TeamMember m where m.id = :id and m.creatorId = :creatorId", TeamMember.class)
                .setParameter("id", id).setParameter("creatorId", creatorId).uniqueResultOptional();
    }

    @Override
    public Optional<TeamMember> findByCreatorIdAndMemberCreatorId(Long creatorId, Long memberCreatorId) {
        return session().createSelectionQuery("from TeamMember m where m.creatorId = :creatorId and m.memberCreatorId = :memberCreatorId",
                TeamMember.class).setParameter("creatorId", creatorId).setParameter("memberCreatorId", memberCreatorId).uniqueResultOptional();
    }

    @Override
    public Optional<TeamMember> findByMemberCreatorIdAndActiveTrue(Long memberCreatorId) {
        return session().createSelectionQuery("from TeamMember m where m.memberCreatorId = :memberCreatorId and m.active = true", TeamMember.class)
                .setParameter("memberCreatorId", memberCreatorId).uniqueResultOptional();
    }

    @Override
    public List<TeamMember> findByCreatorIdOrderByCreatedAtDesc(Long creatorId) {
        return session().createSelectionQuery("from TeamMember m where m.creatorId = :creatorId order by m.createdAt desc", TeamMember.class)
                .setParameter("creatorId", creatorId).getResultList();
    }
}
