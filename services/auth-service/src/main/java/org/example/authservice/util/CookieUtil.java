package org.example.authservice.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.time.Instant;

public class CookieUtil {
  public static void addRefreshCookie(HttpServletResponse resp, Long userId, String token, Instant exp) {
    ResponseCookie refresh = ResponseCookie.from("refreshToken", token)
      .httpOnly(true)
      .secure(true)
      .sameSite("Strict")
      .path("/")
      .maxAge(Duration.between(Instant.now(), exp))
      .build();

    ResponseCookie uid = ResponseCookie.from("userId", String.valueOf(userId))
      .httpOnly(false)
      .secure(true)
      .sameSite("Strict")
      .path("/")
      .maxAge(Duration.between(Instant.now(), exp))
      .build();

    resp.addHeader(HttpHeaders.SET_COOKIE, refresh.toString());
    resp.addHeader(HttpHeaders.SET_COOKIE, uid.toString());
  }

  public static void clearRefreshCookie(HttpServletResponse resp) {
    ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
      .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(0).build();
    resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
