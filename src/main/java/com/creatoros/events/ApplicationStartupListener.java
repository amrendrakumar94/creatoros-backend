package com.creatoros.events;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.creatoros.util.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private final EmailService emailService;

    @Value("${app.mail.from}")
    private String             notifyEmail;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            log.info("ApplicationStartupListener.onApplicationEvent");
            String startedAt = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss z"));
            emailService.sendEmail(notifyEmail, "CreatorOS backend is up", "The backend finished starting up at " + startedAt + ".");
        } catch (Exception e) {
            log.error("Error in onApplicationEvent ", e);
        }
    }
}
