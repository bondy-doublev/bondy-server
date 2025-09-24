package org.example.authservice.service.interfaces;

import org.example.authservice.entity.OtpCode;

public interface IOtpService {
    record OtpResult(String raw, OtpCode entity) {}
    OtpResult generateOtpCode(String subjectType, Long subjectId, String purpose);
    void validateOtp(String raw, Long subjectId, String purpose);
}
