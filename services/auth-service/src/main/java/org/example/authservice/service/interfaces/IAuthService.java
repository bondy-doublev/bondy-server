package org.example.authservice.service.interfaces;

import org.example.authservice.dto.request.*;
import org.example.authservice.dto.response.AuthResponse;
import org.example.authservice.dto.response.MessageResponse;

public interface IAuthService {
  AuthResponse oauth2(OAuth2Request request);

  AuthResponse login(LoginRequest request);

  AuthResponse refreshToken(Long userId, String sessionId, String rawToken);

  MessageResponse registerInit(RegisterRequest request);

  void registerVerify(String email, String rawCode);

  void changePassword(Long userId, ChangePasswordRequest request);

  MessageResponse sendResetPasswordOtp(String email);

  MessageResponse resetPassword(ResetPasswordRequest request);

  AuthResponse logout(long userId);
}
