package org.example.authservice.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.authservice.entity.OtpCode;
import org.example.authservice.property.PropsConfig;
import org.example.authservice.repository.OtpRepository;
import org.example.authservice.service.interfaces.IOtpService;
import org.example.commonweb.enums.Action;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpService implements IOtpService {
  OtpRepository otpRepo;
  PasswordEncoder passwordEncoder;

  PropsConfig props;

  @Override
  public OtpResult generateOtpCode(String subjectType, Long subjectId, Action purpose) {
    otpRepo.deactivateActiveOtp(subjectId, purpose.name());

    String rawCode = String.format("%06d", (int) (Math.random() * 1_000_000));

    if (!props.getEnvironment().equals("production"))
      rawCode = "111111";

    String codeHash = passwordEncoder.encode(rawCode);

    OtpCode otp = OtpCode.builder()
      .subjectType(subjectType)
      .subjectId(subjectId)
      .purpose(purpose.name())
      .codeHash(codeHash)
      .attempts(0)
      .active(true)
      .expiresAt(LocalDateTime.now().plusMinutes(props.getOtp().getTtlMinutes()))
      .build();

    otpRepo.save(otp);

    return new OtpResult(rawCode, otp);
  }

  @Transactional
  @Override
  public void validateOtp(String raw, Long subjectId, Action purpose) {
    OtpCode otp = otpRepo.findTopBySubjectIdAndPurposeAndActiveTrueOrderByCreatedAtDesc(subjectId, purpose.name()).orElse(null);
    if (otp == null) {
      throw new AppException(ErrorCode.ENTITY_NOT_FOUND,
        "OTP does not exist for this request.");
    }

    final int MAX_ATTEMPTS = props.getOtp().getMaxAttempts();
    final var now = LocalDateTime.now();

    if (!otp.getActive()) {
      throw new AppException(ErrorCode.BAD_REQUEST,
        "OTP is inactive. Please request a new code.");
    }

    if (otp.getExpiresAt().isBefore(now)) {
      otp.setActive(false);
      otpRepo.save(otp);
      throw new AppException(ErrorCode.BAD_REQUEST,
        "OTP has expired. Please request a new code.");
    }

    if (otp.getAttempts() >= MAX_ATTEMPTS) {
      otp.setActive(false);
      otpRepo.save(otp);
      throw new AppException(ErrorCode.BAD_REQUEST,
        "Too many failed attempts. OTP has been locked.");
    }

    if (!passwordEncoder.matches(raw, otp.getCodeHash())) {
      otp.setAttempts(otp.getAttempts() + 1);
      if (otp.getAttempts() >= MAX_ATTEMPTS) {
        otp.setActive(false);
      }
      otpRepo.save(otp);

      int remaining = Math.max(0, MAX_ATTEMPTS - otp.getAttempts());
      String msg = (remaining > 0)
        ? "Incorrect OTP. Attempts remaining: " + remaining + "."
        : "Too many failed attempts. OTP has been locked.";
      throw new AppException(ErrorCode.BAD_REQUEST, msg);
    }

    otp.setActive(false);
    otpRepo.save(otp);
  }
}
