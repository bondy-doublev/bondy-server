package org.example.authservice.service;

import org.example.authservice.client.MailClient;
import org.example.authservice.config.security.JwtService;
import org.example.authservice.DTO.request.LoginRequest;
import org.example.authservice.DTO.request.OAuth2Request;
import org.example.authservice.DTO.request.RegisterRequest;
import org.example.authservice.DTO.response.AuthResponse;
import org.example.authservice.DTO.response.MessageResponse;
import org.example.authservice.entity.*;
import org.example.authservice.property.PropsConfig;
import org.example.authservice.repository.PreRegRepository;
import org.example.authservice.repository.RefreshTokenRepository;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.service.interfaces.IAuthService;
import org.example.authservice.service.interfaces.IOtpService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.request.MailRequest;
import org.example.commonweb.enums.*;
import org.example.commonweb.exception.AppException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService implements IAuthService {
    UserRepository userRepo;
    RefreshTokenRepository refreshTokenRepo;
    PreRegRepository preRegRepo;
    OtpService otpService;

    PasswordEncoder passwordEncoder;
    JwtService tokenSigner;
    MailClient mailClient;

    PropsConfig props;

    @Override
    @Transactional
    public AuthResponse oauth2(OAuth2Request request) {
        Provider oauthProvider = Provider.valueOf(request.getProvider().toUpperCase());

        User user = userRepo.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            User newUser = User.builder()
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .middleName(request.getMiddleName())
                    .lastName(request.getLastName())
                    .avatarUrl(request.getAvatarUrl())
                    .role(Role.USER.name())
                    .build();

            Account oauthAccount = Account.builder()
                    .user(newUser)
                    .provider(oauthProvider.name())
                    .build();
            newUser.addAccount(oauthAccount);

            String rawPassword = generateDefaultPassword(request.getFirstName());
            Account localAccount = Account.builder()
                    .user(newUser)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .provider(Provider.LOCAL.name())
                    .build();
            newUser.addAccount(localAccount);

            userRepo.save(newUser);

            sendWelcomeMail(newUser, rawPassword, oauthProvider);

            return buildAuthResponse(newUser);
        }

        if (user.getAvatarUrl() == null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        boolean hasThisOauth = user.getAccounts() != null &&
                user.getAccounts().stream().anyMatch(a -> oauthProvider.name().equals(a.getProvider()));
        if (!hasThisOauth) {
            Account oauthAccount = Account.builder()
                    .user(user)
                    .provider(oauthProvider.name())
                    .build();
            user.addAccount(oauthAccount);
        }

        boolean hasLocal = user.getAccounts() != null &&
                user.getAccounts().stream().anyMatch(a -> Provider.LOCAL.name().equals(a.getProvider()));
        if (!hasLocal) {
            String rawPassword = generateDefaultPassword(user.getFirstName());
            Account localAccount = Account.builder()
                    .user(user)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .provider(Provider.LOCAL.name())
                    .build();
            user.addAccount(localAccount);

            sendWelcomeMail(user, rawPassword, oauthProvider);
        }

        userRepo.save(user);
        return buildAuthResponse(user);
    }

    private String generateDefaultPassword(String firstName) {
        return props.getEnvironment().equals("production")
                ? firstName + props.getUser().getDefaultPasswordSuffix()
                : "11111111";
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String errorMessage = "Invalid email or password.";

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, errorMessage));

        if (!user.getActive())
            throw new AppException(ErrorCode.UNAUTHORIZED, "Your account is inactive.");

        Account localAccount = user.getAccounts().stream()
                .filter(acc -> Provider.LOCAL.name().equals(acc.getProvider()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, errorMessage));

        if (!passwordEncoder.matches(request.getPassword(), localAccount.getPasswordHash()))
            throw new AppException(ErrorCode.UNAUTHORIZED, errorMessage);

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public MessageResponse registerInit(RegisterRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElse(null);

        if (user != null)
            throw new AppException(ErrorCode.DUPLICATE_RESOURCE, "This email has already been registered.");

        PreRegistration preReg = preRegRepo.findByEmail(request.getEmail()).orElse(null);

        if (preReg == null) {
            preReg = PreRegistration.builder()
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .middleName(request.getMiddleName())
                    .lastName(request.getLastName())
                    .dob(request.getDob())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .gender(request.getGender())
                    .build();
            preRegRepo.save(preReg);
        }

        IOtpService.OtpResult otp = otpService.generateOtpCode("pre_registration", preReg.getId(), Action.REGISTER.name());

        MailRequest mail = MailRequest.builder()
                .to(preReg.getEmail())
                .template(MailPurpose.OTP_REGISTRATION.template)
                .locale(Locale.getDefault())
                .model(Map.of(
                        "firstName", preReg.getFirstName(),
                        "otp", otp.raw(),
                        "expiresMinutes", props.getOtp().getTtlMinutes()
                ))
                .build();

        mailClient.send(mail);

        return new MessageResponse("OTP Code sent to your email.");
    }

    @Override
    @Transactional
    public void registerVerify(String email, String rawCode) {
        PreRegistration preReg = preRegRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST,
                        "Verification information is incorrect. Please restart the registration process."));

        otpService.validateOtp(rawCode, preReg.getId(), Action.REGISTER.name());

        User newUser = User.builder()
                .email(email)
                .firstName(preReg.getFirstName())
                .middleName(preReg.getMiddleName())
                .lastName(preReg.getLastName())
                .dob(preReg.getDob())
                .gender(preReg.getGender())
                .role(Role.USER.name())
                .active(true)
                .build();

        Account newAccount = Account.builder()
                .user(newUser)
                .provider(Provider.LOCAL.name())
                .passwordHash(preReg.getPasswordHash())
                .build();

        newUser.addAccount(newAccount);

        userRepo.save(newUser);

        sendRegistrationWelcomeMail(newUser);

        preRegRepo.delete(preReg);
    }

    private AuthResponse buildAuthResponse(User user) {
        var accessToken = tokenSigner.generateAccessToken(user);
        var refreshToken = generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private RefreshToken generateRefreshToken(User user) {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepo.revokeTokens(user.getId(), LocalDateTime.now());
        refreshTokenRepo.save(token);
        return token;
    }

    private void sendRegistrationWelcomeMail(User user) {
        MailRequest mail = MailRequest.builder()
                .to(user.getEmail())
                .template(MailPurpose.WELCOME.template)
                .locale(Locale.getDefault())
                .model(Map.of(
                        "firstName", user.getFirstName(),
                        "email", user.getEmail()
                ))
                .build();

        mailClient.send(mail);
    }

    private void sendWelcomeMail(User user, String rawPassword, Provider provider) {
        MailRequest mail = MailRequest.builder()
                .to(user.getEmail())
                .template(MailPurpose.OAUTH2_WELCOME.template)
                .locale(Locale.getDefault())
                .model(Map.of(
                        "firstName", user.getFirstName(),
                        "provider", provider.name(),
                        "email", user.getEmail(),
                        "password", rawPassword
                ))
                .build();

        mailClient.send(mail);
    }
}
