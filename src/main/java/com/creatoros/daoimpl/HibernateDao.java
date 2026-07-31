package com.creatoros.daoimpl;

import com.creatoros.config.DatabaseConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;

/**
 * Base class for the Hibernate-backed data access objects.
 *
 * <p>The Session is obtained by unwrapping the container-managed EntityManager rather than via
 * {@code SessionFactory.getCurrentSession()}. Spring Boot's JPA auto-configuration wires a
 * {@code JpaTransactionManager}, which binds an EntityManager to the thread and does not install a
 * Hibernate {@code CurrentSessionContext} - so {@code getCurrentSession()} would throw. Unwrapping
 * yields the Session enlisted in the active {@code @Transactional}, which is what every query here
 * needs.
 */
public abstract class HibernateDao {

    /**
     * Pinned to the {@code creatoros} unit by name rather than relying on it being the only one.
     * A DAO for another database must not silently inherit this unit - it declares its own, e.g.
     * {@code @PersistenceContext(unitName = "audit")}.
     */
    @PersistenceContext(unitName = DatabaseConfig.CREATOROS_UNIT)
    private EntityManager entityManager;

    /** The Hibernate Session bound to the current Spring transaction. */
    protected Session session() {
        return entityManager.unwrap(Session.class);
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
