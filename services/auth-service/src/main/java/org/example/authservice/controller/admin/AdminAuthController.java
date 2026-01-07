package org.example.authservice.controller.admin;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.authservice.dto.RefreshTokenDto;
import org.example.authservice.dto.request.LoginRequest;
import org.example.authservice.dto.response.AuthResponse;
import org.example.authservice.service.interfaces.IAuthService;
import org.example.authservice.util.CookieUtil;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminAuthController {
  IAuthService authService;
  CookieUtil cookieUtil;

  @PostMapping("/login")
  AppApiResponse login(@RequestBody @Valid LoginRequest request,
                       HttpServletResponse response) {
    AuthResponse authResponse = authService.adminLogin(request);

    RefreshTokenDto token = authResponse.getRefreshToken();

    cookieUtil.addRefreshCookie(
      response,
      authResponse.getUser().getId(),
      token.getToken(),
      token.getSessionId(),
      token.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant()
    );

    return new AppApiResponse(authResponse);
  }

  @PostMapping("/logout")
  AppApiResponse logout(@Parameter(hidden = true) @CookieValue(value = "userId") String userId,
                        @Parameter(hidden = true) @CookieValue(value = "sessionId") String sessionId,
                        HttpServletResponse response) {
    AuthResponse res = authService.logout(Long.parseLong(userId), sessionId);

    cookieUtil.clearRefreshCookie(response);

    return new AppApiResponse(res);
  }
}
