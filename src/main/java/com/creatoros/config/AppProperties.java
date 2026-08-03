package com.creatoros.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Strongly-typed binding for the {@code app.*} keys in application.properties.
 */
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private Jwt  jwt  = new Jwt();
    private Otp  otp  = new Otp();
    private Cors cors = new Cors();

    @Getter
    @Setter
    public static class Jwt {
        /**
         * Base64-encoded HS256 signing key; must decode to at least 32 bytes.
         */
        private String secret;
        private long   expirationMs = 604_800_000L;
    }

    @Getter
    @Setter
    public static class Otp {
        private int ttlMinutes  = 10;
        private int maxAttempts = 5;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:3000");
    }
}
