package com.creatoros.dto.notification;

import java.math.BigDecimal;
import java.time.Instant;

import com.creatoros.entity.NotificationType;

public record NotificationDto(

        String id,

        String title,

        String message,

        Instant timestamp,

        NotificationType type,

        boolean read,

        String actionUrl,

        BigDecimal amount) {
}
