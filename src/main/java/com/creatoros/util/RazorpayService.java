package com.creatoros.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.creatoros.exception.PaymentGatewayException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Creates Razorpay Payment Links (a hosted page on Razorpay's own domain - no card data ever
 * touches this server) and verifies the HMAC-SHA256 signature on incoming webhooks. Talks to
 * Razorpay's REST API directly via {@link RestClient}, the same pattern {@link EmailService}
 * uses for Brevo, rather than pulling in the official SDK.
 */
@Service
public class RazorpayService {

    private static final String PAYMENT_LINKS_URL = "https://api.razorpay.com/v1/payment_links";
    private static final BigDecimal HUNDRED = new BigDecimal(100);

    private final RestClient restClient = RestClient.create();

    @Value("${app.razorpay.key-id:}")
    private String keyId;

    @Value("${app.razorpay.key-secret:}")
    private String keySecret;

    @Value("${app.razorpay.webhook-secret:}")
    private String webhookSecret;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * {@code customerEmail} may be null - Razorpay just won't send its own notification email.
     * {@code callbackPath} may be null too - when it is, no {@code callback_url} is sent at all,
     * and the payer simply sees Razorpay's own "payment successful" page with no redirect back
     * here. That's the right choice for a brand paying an invoice (they have no CreatorOS
     * account to return to); pass a path (e.g. {@code "/settings"}) when the payer is a signed-in
     * creator who should land back in the app.
     */
    public PaymentLink createPaymentLink(BigDecimal amount, String description, String referenceId, String customerName, String customerEmail,
            String callbackPath) {
        long amountInPaise = amount.multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP).longValueExact();
        RazorpayCustomer customer = customerName == null && customerEmail == null ? null
                : new RazorpayCustomer(customerName, customerEmail);
        String callbackUrl = callbackPath == null ? null : frontendUrl + callbackPath;

        PaymentLinkRequest request = new PaymentLinkRequest(amountInPaise, "INR", description, referenceId, customer,
                new NotifyOptions(customerEmail != null), callbackUrl, callbackUrl == null ? null : "get");

        try {
            PaymentLinkResponse response = restClient.post().uri(PAYMENT_LINKS_URL)
                    .headers(headers -> headers.setBasicAuth(keyId, keySecret)).contentType(MediaType.APPLICATION_JSON).body(request).retrieve()
                    .body(PaymentLinkResponse.class);
            return new PaymentLink(response.id(), response.shortUrl());
        } catch (Exception exception) {
            throw new PaymentGatewayException("Unable to create a Razorpay payment link for " + referenceId, exception);
        }
    }

    /**
     * Constant-time comparison against the hex-encoded HMAC-SHA256 of the raw request body -
     * Razorpay signs the exact bytes it sent, so this must run against the untouched body string,
     * never a re-serialised version of a parsed payload.
     */
    public boolean verifyWebhookSignature(String rawBody, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank() || webhookSecret == null || webhookSecret.isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String computed = HexFormat.of().formatHex(mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(computed.getBytes(StandardCharsets.UTF_8), signatureHeader.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new PaymentGatewayException("Unable to verify Razorpay webhook signature", exception);
        }
    }

    public record PaymentLink(String id, String url) {
    }

    private record RazorpayCustomer(String name, String email) {
    }

    private record NotifyOptions(boolean email) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record PaymentLinkRequest(long amount, String currency, String description, @JsonProperty("reference_id") String referenceId,
            RazorpayCustomer customer, @JsonProperty("notify") NotifyOptions notifyOptions, @JsonProperty("callback_url") String callbackUrl,
            @JsonProperty("callback_method") String callbackMethod) {
    }

    private record PaymentLinkResponse(String id, @JsonProperty("short_url") String shortUrl) {
    }
}
