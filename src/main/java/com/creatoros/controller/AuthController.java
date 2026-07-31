package com.creatoros.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creatoros.dto.auth.AuthResponse;
import com.creatoros.dto.auth.EmailOnlyRequest;
import com.creatoros.dto.auth.LoginRequest;
import com.creatoros.dto.auth.MessageResponse;
import com.creatoros.dto.auth.ResetPasswordRequest;
import com.creatoros.dto.auth.SignupRequest;
import com.creatoros.dto.auth.VerifyOtpRequest;
import com.creatoros.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Unauthenticated endpoints. Permitted by SecurityConfig under /api/v1/auth/**.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Verification code sent to " + request.email(), request.email()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifySignupOtp(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<MessageResponse> resendOtp(@Valid @RequestBody EmailOnlyRequest request) {
        authService.resendSignupOtp(request.email());
        return ResponseEntity.ok(new MessageResponse("A new verification code has been sent.", request.email()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody EmailOnlyRequest request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.ok(new MessageResponse("If that email is registered, a reset code has been sent.", request.email()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
