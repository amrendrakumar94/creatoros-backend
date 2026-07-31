package com.creatoros.daoimpl;

import com.creatoros.entity.Notification;
import com.creatoros.dao.NotificationDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NotificationDaoImpl extends HibernateDao implements NotificationDao {

    @Override
    public Notification save(Notification notification) {
        return persistOrMerge(notification, notification.getId());
    }

    @Override
    public void delete(Notification notification) {
        removeEntity(notification);
    }

    @Override
    public List<Notification> findByCreatorIdOrderByCreatedAtDesc(Long creatorId) {
        return session()
                .createSelectionQuery(
                        "from Notification n where n.creator.id = :creatorId order by n.createdAt desc",
                        Notification.class)
                .setParameter("creatorId", creatorId)
                .getResultList();
    }

    /** Scoped lookup: an id belonging to another creator simply is not found. */
    @Override
    public Optional<Notification> findByIdAndCreatorId(Long id, Long creatorId) {
        return session()
                .createSelectionQuery(
                        "from Notification n where n.id = :id and n.creator.id = :creatorId",
                        Notification.class)
                .setParameter("id", id)
                .setParameter("creatorId", creatorId)
                .uniqueResultOptional();
    }

    @Override
    public int markAllRead(Long creatorId) {
        return executeBulk(session()
                .createMutationQuery("""
                        update Notification n
                           set n.read = true
                         where n.creator.id = :creatorId
                           and n.read = false
                        """)
                .setParameter("creatorId", creatorId));
    }
}
