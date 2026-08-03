package com.creatoros.service;

import com.creatoros.dto.auth.AuthResponse;
import com.creatoros.dto.auth.LoginRequest;
import com.creatoros.dto.auth.SignupRequest;

public interface AuthService {

    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}
