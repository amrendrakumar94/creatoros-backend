package com.creatoros.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RazorpayServiceTest {

    private final RazorpayService service = new RazorpayService();

    @Test
    void verifyWebhookSignatureAcceptsAMatchingSignature() throws Exception {
        ReflectionTestUtils.setField(service, "webhookSecret", "test-secret");
        String body = "{\"event\":\"payment_link.paid\"}";

        assertThat(service.verifyWebhookSignature(body, computeSignature("test-secret", body))).isTrue();
    }

    @Test
    void verifyWebhookSignatureRejectsATamperedBody() throws Exception {
        ReflectionTestUtils.setField(service, "webhookSecret", "test-secret");
        String body = "{\"event\":\"payment_link.paid\"}";
        String signature = computeSignature("test-secret", body);

        assertThat(service.verifyWebhookSignature(body + "x", signature)).isFalse();
    }

    @Test
    void verifyWebhookSignatureRejectsTheWrongSecret() throws Exception {
        ReflectionTestUtils.setField(service, "webhookSecret", "test-secret");
        String body = "{\"event\":\"payment_link.paid\"}";

        assertThat(service.verifyWebhookSignature(body, computeSignature("wrong-secret", body))).isFalse();
    }

    @Test
    void verifyWebhookSignatureRejectsAMissingOrBlankHeader() {
        ReflectionTestUtils.setField(service, "webhookSecret", "test-secret");

        assertThat(service.verifyWebhookSignature("{}", null)).isFalse();
        assertThat(service.verifyWebhookSignature("{}", "")).isFalse();
    }

    @Test
    void verifyWebhookSignatureRejectsWhenNoSecretIsConfigured() throws Exception {
        ReflectionTestUtils.setField(service, "webhookSecret", "");
        String body = "{}";

        assertThat(service.verifyWebhookSignature(body, computeSignature("anything", body))).isFalse();
    }

    private static String computeSignature(String secret, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }
}
