package org.example.authservice.service.interfaces;

import org.example.authservice.entity.OtpCode;
import org.example.commonweb.enums.Action;

public interface IOtpService {
    record OtpResult(String raw, OtpCode entity) {}
    OtpResult generateOtpCode(String subjectType, Long subjectId, Action purpose);
    void validateOtp(String raw, Long subjectId, Action purpose);
}
