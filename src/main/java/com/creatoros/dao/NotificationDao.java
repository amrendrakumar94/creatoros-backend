package com.creatoros.dao;

import java.util.List;
import java.util.Optional;

import com.creatoros.entity.Notification;

public interface NotificationDao {

    Notification save(Notification notification);

    void delete(Notification notification);

    List<Notification> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    Optional<Notification> findByIdAndCreatorId(Long id, Long creatorId);

    int markAllRead(Long creatorId);
}
