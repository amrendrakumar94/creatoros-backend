package com.creatoros.controller;

import com.creatoros.dto.notification.NotificationDto;
import com.creatoros.security.SecurityUtils;
import com.creatoros.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> list() {
        return ResponseEntity.ok(notificationService.listForCreator(SecurityUtils.currentCreatorId()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markRead(SecurityUtils.currentCreatorId(), id));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllRead() {
        int updated = notificationService.markAllRead(SecurityUtils.currentCreatorId());
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.delete(SecurityUtils.currentCreatorId(), id);
        return ResponseEntity.noContent().build();
    }
}
