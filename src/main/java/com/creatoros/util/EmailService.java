package com.creatoros.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Sends through Brevo's HTTPS transactional email API rather than SMTP - outbound SMTP ports are
 * blocked on the host this runs on, so a mail relay connection never gets past the TCP handshake.
 */
@Service
public class EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestClient restClient = RestClient.create();

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name:CreatorOS}")
    private String fromName;

    @Value("${app.mail.brevo-api-key:}")
    private String apiKey;

    public void sendEmail(String to, String subject, String body) {
        try {
            restClient.post().uri(BREVO_API_URL).header("api-key", apiKey).contentType(MediaType.APPLICATION_JSON)
                    .body(new BrevoEmailRequest(new BrevoSender(fromName, fromEmail), List.of(new BrevoRecipient(to)), subject, body)).retrieve()
                    .toBodilessEntity();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to send email to " + to, exception);
        }
    }

    private record BrevoSender(String name, String email) {
    }

    private record BrevoRecipient(String email) {
    }

    private record BrevoEmailRequest(BrevoSender sender, List<BrevoRecipient> to, String subject, String textContent) {
    }
}
