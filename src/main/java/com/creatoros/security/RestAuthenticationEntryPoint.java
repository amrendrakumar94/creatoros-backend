package com.creatoros.security;

import com.creatoros.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Returns a JSON {@link ApiError} for unauthenticated requests instead of
 * Spring Security's default HTML login redirect, so the frontend's fetch client
 * can parse every failure uniformly.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError body = ApiError.of(HttpStatus.UNAUTHORIZED, "Authentication required", "UNAUTHENTICATED", request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
