package com.creatoros.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creatoros.service.RazorpayWebhookService;
import com.creatoros.util.RazorpayService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * Public endpoint Razorpay calls directly - permitted unauthenticated in {@code SecurityConfig}.
 * The signature check here is what stands in for authentication, so it must run against the raw
 * body exactly as received, before any parsing.
 */
@RestController
@RequestMapping("/api/v1/webhooks/razorpay")
@RequiredArgsConstructor
@Slf4j
public class RazorpayWebhookController {

    private final RazorpayService        razorpayService;
    private final RazorpayWebhookService webhookService;
    private final ObjectMapper           objectMapper;

    @PostMapping
    public ResponseEntity<Void> handle(@RequestBody String rawBody, @RequestHeader("X-Razorpay-Signature") String signature) {
        if (!razorpayService.verifyWebhookSignature(rawBody, signature)) {
            log.warn("Rejected a Razorpay webhook with an invalid signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        webhookService.handleEvent(objectMapper.readTree(rawBody));
        return ResponseEntity.ok().build();
    }
}
