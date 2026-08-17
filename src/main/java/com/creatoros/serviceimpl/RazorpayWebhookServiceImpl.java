package com.creatoros.serviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.creatoros.service.InvoiceService;
import com.creatoros.service.RazorpayWebhookService;
import com.creatoros.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * Routes a verified Razorpay webhook event to whichever domain it's for, keyed off the
 * {@code reference_id} the payment link was created with ({@code invoice:<id>} or
 * {@code subscription:<creatorId>}) - the one piece of routing information Razorpay echoes back
 * unchanged on every event for that link.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RazorpayWebhookServiceImpl implements RazorpayWebhookService {

    private static final String INVOICE_PREFIX      = "invoice:";
    private static final String SUBSCRIPTION_PREFIX = "subscription:";
    private static final BigDecimal HUNDRED         = new BigDecimal(100);

    private final InvoiceService      invoiceService;
    private final SubscriptionService subscriptionService;

    @Override
    public void handleEvent(JsonNode payload) {
        String event = payload.path("event").asString("");
        if (!"payment_link.paid".equals(event)) {
            log.info("Ignoring unhandled Razorpay webhook event: {}", event);
            return;
        }

        JsonNode linkEntity = payload.path("payload").path("payment_link").path("entity");
        JsonNode paymentEntity = payload.path("payload").path("payment").path("entity");

        String referenceId = linkEntity.path("reference_id").asString("");
        String razorpayPaymentId = paymentEntity.path("id").asString("");
        BigDecimal amount = new BigDecimal(paymentEntity.path("amount").asLong(0)).divide(HUNDRED, 2, RoundingMode.UNNECESSARY);

        if (referenceId.startsWith(INVOICE_PREFIX)) {
            invoiceService.recordGatewayPayment(parseId(referenceId, INVOICE_PREFIX), amount, razorpayPaymentId);
        } else if (referenceId.startsWith(SUBSCRIPTION_PREFIX)) {
            subscriptionService.activateFromGatewayPayment(parseId(referenceId, SUBSCRIPTION_PREFIX), razorpayPaymentId);
        } else {
            log.warn("Razorpay webhook payment_link.paid with an unrecognised reference_id: {}", referenceId);
        }
    }

    private Long parseId(String referenceId, String prefix) {
        return Long.parseLong(referenceId.substring(prefix.length()));
    }
}
