package org.example.authservice.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.property.PropsConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieUtil {

  private final PropsConfig props;

  private boolean isProd() {
    return "production".equalsIgnoreCase(props.getEnvironment());
  }

  public void addRefreshCookie(
    HttpServletResponse resp,
    Long userId,
    String token,
    Instant exp
  ) {

    Duration maxAge = Duration.between(Instant.now(), exp);

    boolean isProd = isProd();

    System.out.println("environment: " + isProd);

    ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", token)
      .httpOnly(true)
      .secure(isProd)                     // PROD=true, LOCAL=false
      .sameSite(isProd ? "None" : "Lax")  // PROD=None, LOCAL=Lax
      .path("/")
      .maxAge(maxAge)
      .build();

    ResponseCookie userIdCookie = ResponseCookie.from("userId", String.valueOf(userId))
      .httpOnly(false)
      .secure(isProd)
      .sameSite(isProd ? "None" : "Lax")
      .path("/")
      .maxAge(maxAge)
      .build();

    resp.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    resp.addHeader(HttpHeaders.SET_COOKIE, userIdCookie.toString());
  }

  public void clearRefreshCookie(HttpServletResponse resp) {

    boolean isProd = isProd();

    ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
      .httpOnly(true)
      .secure(isProd)
      .sameSite(isProd ? "None" : "Lax")
      .path("/")
      .maxAge(0)
      .build();

    ResponseCookie userIdCookie = ResponseCookie.from("userId", "")
      .httpOnly(false)
      .secure(isProd)
      .sameSite(isProd ? "None" : "Lax")
      .path("/")
      .maxAge(0)
      .build();

    resp.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    resp.addHeader(HttpHeaders.SET_COOKIE, userIdCookie.toString());
  }
}
