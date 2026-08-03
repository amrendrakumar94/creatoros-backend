package com.creatoros.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatoros.dao.NotificationDao;
import com.creatoros.dto.notification.NotificationDto;
import com.creatoros.entity.Creator;
import com.creatoros.entity.Notification;
import com.creatoros.enums.NotificationType;
import com.creatoros.exception.ResourceNotFoundException;
import com.creatoros.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationDao notificationDao;
    private final DomainMapper    domainMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> listForCreator(Long creatorId) {
        return notificationDao.findByCreatorIdOrderByCreatedAtDesc(creatorId).stream().map(domainMapper::toNotificationDto).toList();
    }

    @Override
    @Transactional
    public NotificationDto markRead(Long creatorId, Long notificationId) {
        Notification notification = notificationDao.findByIdAndCreatorId(notificationId, creatorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", notificationId));
        notification.setRead(true);
        return domainMapper.toNotificationDto(notificationDao.save(notification));
    }

    @Override
    @Transactional
    public int markAllRead(Long creatorId) {
        return notificationDao.markAllRead(creatorId);
    }

    @Override
    @Transactional
    public void delete(Long creatorId, Long notificationId) {
        Notification notification = notificationDao.findByIdAndCreatorId(notificationId, creatorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", notificationId));
        notificationDao.delete(notification);
    }

    @Override
    @Transactional
    public void record(Creator creator, NotificationType type, String title, String message, String actionUrl, BigDecimal amount) {
        Notification notification = Notification.builder().creator(creator).type(type).title(title).message(message).actionUrl(actionUrl)
                .amount(amount).read(false).build();
        notificationDao.save(notification);
        log.debug("Recorded {} notification for creator {}", type, creator.getId());
    }
}
