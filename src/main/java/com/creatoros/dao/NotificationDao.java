package com.creatoros.dao;

import com.creatoros.entity.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationDao {

    Notification save(Notification notification);

    void delete(Notification notification);

    List<Notification> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    /** Scoped lookup: an id belonging to another creator simply is not found. */
    Optional<Notification> findByIdAndCreatorId(Long id, Long creatorId);

    /**
     * Marks every unread notification read in one statement.
     *
     * <p>Bulk update: flushes first and clears the session afterwards.
     */
    int markAllRead(Long creatorId);
}
