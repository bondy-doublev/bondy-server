package org.example.authservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.authservice.DTO.request.LoginRequest;
import org.example.authservice.DTO.request.OAuth2Request;
import org.example.authservice.DTO.request.OtpVerifyRequest;
import org.example.authservice.DTO.request.RegisterRequest;
import org.example.authservice.DTO.response.AuthResponse;
import org.example.authservice.DTO.response.MessageResponse;
import org.example.authservice.entity.RefreshToken;
import org.example.authservice.service.interfaces.IAuthService;
import org.example.authservice.util.CookieUtil;
import org.example.commonweb.DTO.core.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    IAuthService authService;

    @PostMapping("/login")
    ApiResponse login(@RequestBody @Valid LoginRequest request,
                      HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);

        RefreshToken token = authResponse.getRefreshToken();

        CookieUtil.addRefreshCookie(response,
                token.getToken(),
                token.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant());

        return new ApiResponse(authResponse);
    }

    @PostMapping("/oauth2")
    ApiResponse oauth2(@RequestBody @Valid OAuth2Request request,
                           HttpServletResponse response) {
        AuthResponse authResponse = authService.oauth2(request);

        RefreshToken token = authResponse.getRefreshToken();

        CookieUtil.addRefreshCookie(response,
                token.getToken(),
                token.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant());

        return new ApiResponse(authResponse);
    }

    @PostMapping("/register/init")
    ApiResponse registerInit(@RequestBody @Valid RegisterRequest request) {
        MessageResponse response = authService.registerInit(request);
        return new ApiResponse(response);
    }

    @PostMapping("/register/verify")
    ApiResponse registerVerify(@RequestBody @Valid OtpVerifyRequest request) {
        authService.registerVerify(request.getEmail(), request.getOtpCode());
        return new ApiResponse();
    }
}
