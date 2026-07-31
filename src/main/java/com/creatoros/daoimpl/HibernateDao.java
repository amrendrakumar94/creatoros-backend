package com.creatoros.daoimpl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class HibernateDao {

    @Autowired
    private SessionFactory sessionFactory;

    protected Session session() {
        return sessionFactory.getCurrentSession();
    }

    protected <T> T persistOrMerge(T entity, Object id) {
        if (id == null) {
            session().persist(entity);
            return entity;
        }
        return session().merge(entity);
    }

    protected void removeEntity(Object entity) {
        Session session = session();
        session.remove(session.contains(entity) ? entity : session.merge(entity));
    }

    protected int executeBulk(org.hibernate.query.MutationQuery query) {
        session().flush();
        int updated = query.executeUpdate();
        session().clear();
        return updated;
    }
}
