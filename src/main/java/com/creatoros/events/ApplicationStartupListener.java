package com.creatoros.events;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            log.info("ApplicationStartupListener.onApplicationEvent");
        } catch (Exception e) {
            log.error("Error in onApplicationEvent ", e);
        }
    }
}
