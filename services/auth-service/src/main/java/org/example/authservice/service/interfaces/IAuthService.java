package org.example.authservice.service.interfaces;

import org.example.authservice.dto.request.LoginRequest;
import org.example.authservice.dto.request.OAuth2Request;
import org.example.authservice.dto.request.RegisterRequest;
import org.example.authservice.dto.response.AuthResponse;
import org.example.authservice.dto.response.MessageResponse;

public interface IAuthService {
    AuthResponse oauth2(OAuth2Request request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String rawToken);
    MessageResponse registerInit(RegisterRequest request);
    void registerVerify(String email, String rawCode);
}
