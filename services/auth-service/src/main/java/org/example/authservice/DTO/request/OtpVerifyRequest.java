package org.example.authservice.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpVerifyRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "Otp code is required")
    @Size(min = 6, max = 6, message = "Otp code must be exactly 6 digits")
    @Pattern(regexp = "\\d{6}", message = "Otp code must contain only digits")
    private String otpCode;
}
