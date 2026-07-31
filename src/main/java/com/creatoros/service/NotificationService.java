package com.creatoros.service;

import com.creatoros.dto.notification.NotificationDto;
import com.creatoros.entity.Creator;
import com.creatoros.entity.NotificationType;

import java.math.BigDecimal;
import java.util.List;

public interface NotificationService {

    List<NotificationDto> listForCreator(Long creatorId);

    NotificationDto markRead(Long creatorId, Long notificationId);

    int markAllRead(Long creatorId);

    void delete(Long creatorId, Long notificationId);

    /** Raised by other services when something noteworthy happens to a creator's records. */
    void record(Creator creator, NotificationType type, String title, String message,
                String actionUrl, BigDecimal amount);
}
