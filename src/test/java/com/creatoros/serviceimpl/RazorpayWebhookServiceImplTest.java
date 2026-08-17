package com.creatoros.serviceimpl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creatoros.service.InvoiceService;
import com.creatoros.service.SubscriptionService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RazorpayWebhookServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private SubscriptionService subscriptionService;

    private RazorpayWebhookServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RazorpayWebhookServiceImpl(invoiceService, subscriptionService);
    }

    @Test
    @DisplayName("routes a payment_link.paid event for an invoice to InvoiceService")
    void routesInvoicePayment() {
        JsonNode payload = objectMapper.readTree(paymentLinkPaidJson("invoice:42", "pay_123", 50000));

        service.handleEvent(payload);

        verify(invoiceService).recordGatewayPayment(42L, new BigDecimal("500.00"), "pay_123");
        verifyNoInteractions(subscriptionService);
    }

    @Test
    @DisplayName("routes a payment_link.paid event for a subscription to SubscriptionService")
    void routesSubscriptionPayment() {
        JsonNode payload = objectMapper.readTree(paymentLinkPaidJson("subscription:7", "pay_456", 99900));

        service.handleEvent(payload);

        verify(subscriptionService).activateFromGatewayPayment(7L, "pay_456");
        verifyNoInteractions(invoiceService);
    }

    @Test
    @DisplayName("ignores events other than payment_link.paid")
    void ignoresUnhandledEvents() {
        JsonNode payload = objectMapper.readTree("{\"event\":\"payment_link.cancelled\"}");

        service.handleEvent(payload);

        verifyNoInteractions(invoiceService, subscriptionService);
    }

    @Test
    @DisplayName("ignores an unrecognised reference_id prefix")
    void ignoresUnrecognisedReferenceId() {
        JsonNode payload = objectMapper.readTree(paymentLinkPaidJson("something-else:1", "pay_789", 10000));

        service.handleEvent(payload);

        verifyNoInteractions(invoiceService, subscriptionService);
    }

    private static String paymentLinkPaidJson(String referenceId, String paymentId, long amountInPaise) {
        return """
                {
                  "event": "payment_link.paid",
                  "payload": {
                    "payment_link": { "entity": { "reference_id": "%s" } },
                    "payment": { "entity": { "id": "%s", "amount": %d } }
                  }
                }
                """.formatted(referenceId, paymentId, amountInPaise);
    }
}
