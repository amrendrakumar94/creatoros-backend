package com.creatoros.service;

import java.math.BigDecimal;
import java.util.List;

import com.creatoros.dto.notification.NotificationDto;
import com.creatoros.entity.Creator;
import com.creatoros.entity.NotificationType;

public interface NotificationService {

    List<NotificationDto> listForCreator(Long creatorId);

    NotificationDto markRead(Long creatorId, Long notificationId);

    int markAllRead(Long creatorId);

    void delete(Long creatorId, Long notificationId);

    void record(Creator creator, NotificationType type, String title, String message, String actionUrl, BigDecimal amount);
}
