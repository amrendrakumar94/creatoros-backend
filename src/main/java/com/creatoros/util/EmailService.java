package com.creatoros.util;

import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonInclude;

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
        sendEmail(new BrevoEmailRequest(new BrevoSender(fromName, fromEmail), List.of(new BrevoRecipient(to)), subject, body, null, null), to);
    }

    /** {@code pdfAttachment} may be null to send an HTML email with no attachment. */
    public void sendEmail(String to, String subject, String textContent, String htmlContent, PdfAttachment pdfAttachment) {
        List<BrevoAttachment> attachments = pdfAttachment == null ? null
                : List.of(new BrevoAttachment(Base64.getEncoder().encodeToString(pdfAttachment.content()), pdfAttachment.filename()));
        sendEmail(new BrevoEmailRequest(new BrevoSender(fromName, fromEmail), List.of(new BrevoRecipient(to)), subject, textContent, htmlContent,
                attachments), to);
    }

    private void sendEmail(BrevoEmailRequest request, String to) {
        try {
            restClient.post().uri(BREVO_API_URL).header("api-key", apiKey).contentType(MediaType.APPLICATION_JSON).body(request).retrieve()
                    .toBodilessEntity();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to send email to " + to, exception);
        }
    }

    public record PdfAttachment(String filename, byte[] content) {
    }

    private record BrevoSender(String name, String email) {
    }

    private record BrevoRecipient(String email) {
    }

    private record BrevoAttachment(String content, String name) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record BrevoEmailRequest(BrevoSender sender, List<BrevoRecipient> to, String subject, String textContent, String htmlContent,
            List<BrevoAttachment> attachment) {
    }
}
