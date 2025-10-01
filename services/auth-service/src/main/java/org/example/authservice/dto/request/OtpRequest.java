package org.example.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;
}
