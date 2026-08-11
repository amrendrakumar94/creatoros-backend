package com.creatoros.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creatoros.dto.subscription.SubscriptionDto;
import com.creatoros.security.SecurityUtils;
import com.creatoros.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<SubscriptionDto> current() {
        return ResponseEntity.ok(subscriptionService.getCurrent(SecurityUtils.currentTenantId()));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionDto> subscribe() {
        return ResponseEntity.ok(subscriptionService.subscribe(SecurityUtils.currentTenantId()));
    }

    @PostMapping("/cancel")
    public ResponseEntity<SubscriptionDto> cancel() {
        return ResponseEntity.ok(subscriptionService.cancel(SecurityUtils.currentTenantId()));
    }
}
