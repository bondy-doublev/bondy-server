package org.example.authservice.service.interfaces;

import org.example.authservice.DTO.request.LoginRequest;
import org.example.authservice.DTO.request.OAuth2Request;
import org.example.authservice.DTO.request.RegisterRequest;
import org.example.authservice.DTO.response.AuthResponse;
import org.example.authservice.DTO.response.MessageResponse;

public interface IAuthService {
    AuthResponse oauth2(OAuth2Request request);
    AuthResponse login(LoginRequest request);
    MessageResponse registerInit(RegisterRequest request);
    void registerVerify(String email, String rawCode);
}
