package com.creatoros.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables {@code @Scheduled} jobs.
 *
 * <p>Separate from the job itself so tests can load a slice without starting the scheduler.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
