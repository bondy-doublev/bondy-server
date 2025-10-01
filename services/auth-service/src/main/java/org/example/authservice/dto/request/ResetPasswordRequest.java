package org.example.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 24, message = "New password must be between 8 and 24 characters")
    String newPassword;

    @NotBlank(message = "New password is required")
    String confirmPassword;

    @Size(min = 6, max = 6, message = "Otp code must be exactly 6 digits")
    @Pattern(regexp = "\\d{6}", message = "Otp code must contain only digits")
    String otpCode;
}
