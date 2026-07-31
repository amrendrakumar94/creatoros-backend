package com.creatoros.daoimpl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for the Hibernate-backed data access objects.
 *
 * <p>Because the application uses {@code HibernateTransactionManager} rather than JPA's
 * {@code JpaTransactionManager}, {@link SessionFactory#getCurrentSession()} returns the Session
 * bound to the active {@code @Transactional} - there is no EntityManager to unwrap. It throws if
 * called with no transaction in progress, which is the behaviour we want: every DAO call must be
 * inside a service-level transaction.
 *
 * <p>Field injection is used deliberately: constructor injection here would force all six
 * subclasses to declare a constructor purely to pass the factory upwards.
 */
public abstract class HibernateDao {

    /**
     * The primary ({@code mainSessionFactory}) unit. A DAO for another database must override this
     * by declaring its own {@code @Qualifier}-ed factory rather than inheriting this one.
     */
    @Autowired
    private SessionFactory sessionFactory;

    /** The Hibernate Session bound to the current transaction. */
    protected Session session() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * Persists a new entity or merges a detached one - a null id is what distinguishes the two.
     *
     * <p>Always use the returned instance: {@code merge} returns the managed copy rather than
     * mutating the argument.
     */
    protected <T> T persistOrMerge(T entity, Object id) {
        if (id == null) {
            // persist() on an IDENTITY column assigns the id immediately, which callers that read
            // entity.getId() straight after saving depend on.
            session().persist(entity);
            return entity;
        }
        return session().merge(entity);
    }

    /**
     * Deletes an entity, re-attaching it first if the caller handed us a detached instance -
     * {@code remove} only accepts managed ones. Configured cascades still apply.
     */
    protected void removeEntity(Object entity) {
        Session session = session();
        session.remove(session.contains(entity) ? entity : session.merge(entity));
    }

    /**
     * Flushes before, and clears after, a bulk mutation.
     *
     * <p>Bulk updates bypass the persistence context. Without the flush, pending changes could be
     * written after the update and undo it; without the clear, entities already loaded would keep
     * their stale pre-update state. Spring Data expressed this as
     * {@code @Modifying(clearAutomatically = true, flushAutomatically = true)}.
     */
    protected int executeBulk(org.hibernate.query.MutationQuery query) {
        session().flush();
        int updated = query.executeUpdate();
        session().clear();
        return updated;
    }
}
