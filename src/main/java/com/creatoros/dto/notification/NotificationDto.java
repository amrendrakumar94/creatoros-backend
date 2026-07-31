package com.creatoros.dto.notification;

import com.creatoros.entity.NotificationType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mirrors {@code NotificationItem} in src/types/creatorOS.ts.
 *
 * <p>{@code timestamp} is an ISO instant rather than a pre-rendered phrase like "10 mins ago" -
 * relative formatting is presentation and belongs on the client, where it also stays correct as
 * the page ages.
 */
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
