package com.creatoros.service;

import com.creatoros.dto.auth.AuthResponse;
import com.creatoros.dto.auth.LoginRequest;
import com.creatoros.dto.auth.ResetPasswordRequest;
import com.creatoros.dto.auth.SignupRequest;
import com.creatoros.dto.auth.VerifyOtpRequest;

public interface AuthService {

    /**
     * Creates a PENDING creator and issues a signup OTP. No session is granted
     * yet.
     */
    void signup(SignupRequest request);

    /**
     * Redeems a signup OTP, activates the account, and returns a session token.
     */
    AuthResponse verifySignupOtp(VerifyOtpRequest request);

    /** Re-issues a signup OTP for an account still awaiting verification. */
    void resendSignupOtp(String email);

    AuthResponse login(LoginRequest request);

    /**
     * Issues a password-reset OTP. Returns normally even for an unknown email
     * so the endpoint cannot be used to discover which addresses are
     * registered.
     */
    void forgotPassword(String email);

    /**
     * Redeems a reset OTP and sets the new password. Returns a fresh session.
     */
    AuthResponse resetPassword(ResetPasswordRequest request);
}
