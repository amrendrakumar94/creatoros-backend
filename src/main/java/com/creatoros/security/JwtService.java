package com.creatoros.security;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.creatoros.config.AppProperties;
import com.creatoros.entity.Creator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE  = "role";

    private final AppProperties appProperties;

    private SecretKey signingKey() {
        String secret = appProperties.getJwt().getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret is not configured - set it in application.properties");
        }
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public Instant expiryFromNow() {
        return Instant.now().plusMillis(appProperties.getJwt().getExpirationMs());
    }

    public String generateToken(Creator creator) {
        Instant now = Instant.now();
        return Jwts.builder().subject(String.valueOf(creator.getId())).claim(CLAIM_EMAIL, creator.getEmail())
                .claim(CLAIM_ROLE, creator.getRole().name()).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(appProperties.getJwt().getExpirationMs()))).signWith(signingKey()).compact();
    }

    public Optional<Long> extractCreatorId(String token) {
        return parse(token).map(claims -> Long.valueOf(claims.getSubject()));
    }

    public Optional<String> extractEmail(String token) {
        return parse(token).map(claims -> claims.get(CLAIM_EMAIL, String.class));
    }

    private Optional<Claims> parse(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Rejected JWT: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
