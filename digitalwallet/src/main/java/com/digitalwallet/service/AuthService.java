package com.digitalwallet.service;

import com.digitalwallet.dto.request.LoginRequest;
import com.digitalwallet.dto.request.RegisterRequest;
import com.digitalwallet.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);
}
