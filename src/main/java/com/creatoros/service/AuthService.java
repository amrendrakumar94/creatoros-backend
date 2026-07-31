package com.creatoros.service;

import com.creatoros.dto.auth.AuthResponse;
import com.creatoros.dto.auth.LoginRequest;
import com.creatoros.dto.auth.ResetPasswordRequest;
import com.creatoros.dto.auth.SignupRequest;
import com.creatoros.dto.auth.VerifyOtpRequest;

public interface AuthService {

    void signup(SignupRequest request);

    AuthResponse verifySignupOtp(VerifyOtpRequest request);

    void resendSignupOtp(String email);

    AuthResponse login(LoginRequest request);

    void forgotPassword(String email);

    AuthResponse resetPassword(ResetPasswordRequest request);
}
