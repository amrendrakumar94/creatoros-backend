package com.creatoros.repository;

import com.creatoros.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    Optional<Notification> findByIdAndCreatorId(Long id, Long creatorId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Notification n SET n.read = true WHERE n.creator.id = :creatorId AND n.read = false")
    int markAllRead(@Param("creatorId") Long creatorId);
}
