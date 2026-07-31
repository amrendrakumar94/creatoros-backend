package com.creatoros.daoimpl;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.creatoros.dao.CreatorDao;
import com.creatoros.entity.Creator;

@Repository
public class CreatorDaoImpl extends HibernateDao implements CreatorDao {

    @Override
    public Creator save(Creator creator) {
        return persistOrMerge(creator, creator.getId());
    }

    @Override
    public Optional<Creator> findById(Long id) {
        return Optional.ofNullable(session().find(Creator.class, id));
    }

    @Override
    public Optional<Creator> findByEmailIgnoreCase(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return session().createSelectionQuery("from Creator c where lower(c.email) = :email", Creator.class)
                .setParameter("email", email.toLowerCase(Locale.ROOT)).uniqueResultOptional();
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        if (email == null) {
            return false;
        }
        return session().createSelectionQuery("select count(c.id) from Creator c where lower(c.email) = :email", Long.class)
                .setParameter("email", email.toLowerCase(Locale.ROOT)).getSingleResult() > 0;
    }

    @Override
    public boolean existsByHandle(String handle) {
        return session().createSelectionQuery("select count(c.id) from Creator c where c.handle = :handle", Long.class).setParameter("handle", handle)
                .getSingleResult() > 0;
    }
}
