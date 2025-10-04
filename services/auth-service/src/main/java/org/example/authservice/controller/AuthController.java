package org.example.authservice.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.authservice.config.security.ContextUser;
import org.example.authservice.dto.request.*;
import org.example.authservice.dto.response.AuthResponse;
import org.example.authservice.dto.response.MessageResponse;
import org.example.authservice.dto.RefreshTokenDto;
import org.example.authservice.service.interfaces.IAuthService;
import org.example.authservice.util.CookieUtil;
import org.example.commonweb.DTO.core.ApiResponse;
import org.springframework.web.bind.annotation.*;

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
                      HttpServletResponse response)
    {
        AuthResponse authResponse = authService.login(request);

        RefreshTokenDto token = authResponse.getRefreshToken();

        CookieUtil.addRefreshCookie(
                response,
                token.getToken(),
                token.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant()
        );

        return new ApiResponse(authResponse);
    }

    @PostMapping("/oauth2")
    ApiResponse oauth2(@RequestBody @Valid OAuth2Request request,
                           HttpServletResponse response)
    {
        AuthResponse authResponse = authService.oauth2(request);

        RefreshTokenDto token = authResponse.getRefreshToken();

        CookieUtil.addRefreshCookie(
                response,
                token.getToken(),
                token.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant()
        );

        return new ApiResponse(authResponse);
    }

    @PostMapping("/refresh")
    ApiResponse refreshToken(@Parameter(hidden = true) @CookieValue(value = "refreshToken") String refreshToken,
                             HttpServletResponse response)
    {
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        RefreshTokenDto token = authResponse.getRefreshToken();

        CookieUtil.addRefreshCookie(
                response,
                token.getToken(),
                token.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant()
        );

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

    @PostMapping("/change-password")
    ApiResponse changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        authService.changePassword(ContextUser.get().getUserId(), request);

        return new ApiResponse(new MessageResponse("Change password successfully."));
    }

    @PostMapping("/reset-password-otp")
    ApiResponse sendOtp(@RequestBody @Valid OtpRequest request) {
        MessageResponse response = authService.sendResetPasswordOtp(request.getEmail());

        return new ApiResponse(response);
    }

    @PostMapping("/reset-password")
    ApiResponse resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);

        return new ApiResponse(response);
    }
}
