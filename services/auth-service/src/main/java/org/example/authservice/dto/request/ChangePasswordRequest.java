package org.example.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @NotBlank(message = "Old password is required")
    String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 24, message = "New password must be between 8 and 24 characters")
    String newPassword;

    @NotBlank(message = "Confirm password is required")
    String confirmPassword;
}
