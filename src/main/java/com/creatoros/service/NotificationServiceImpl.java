package com.creatoros.service;

import com.creatoros.dto.notification.NotificationDto;
import com.creatoros.entity.Creator;
import com.creatoros.entity.Notification;
import com.creatoros.entity.NotificationType;
import com.creatoros.exception.ResourceNotFoundException;
import com.creatoros.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final DomainMapper domainMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> listForCreator(Long creatorId) {
        return notificationRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId).stream()
                .map(domainMapper::toNotificationDto)
                .toList();
    }

    @Override
    @Transactional
    public NotificationDto markRead(Long creatorId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndCreatorId(notificationId, creatorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", notificationId));
        notification.setRead(true);
        return domainMapper.toNotificationDto(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public int markAllRead(Long creatorId) {
        return notificationRepository.markAllRead(creatorId);
    }

    @Override
    @Transactional
    public void delete(Long creatorId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndCreatorId(notificationId, creatorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", notificationId));
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void record(Creator creator, NotificationType type, String title, String message,
                       String actionUrl, BigDecimal amount) {
        Notification notification = Notification.builder()
                .creator(creator)
                .type(type)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .amount(amount)
                .read(false)
                .build();
        notificationRepository.save(notification);
        log.debug("Recorded {} notification for creator {}", type, creator.getId());
    }
}
