package org.example.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OAuth2Request {
    @NotBlank(message = "Provider is required")
    String provider;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "Avatar url is required")
    String avatarUrl;

    @NotBlank(message = "First name is required")
    String firstName;

    String middleName;

    @NotBlank(message = "Last name is required")
    String lastName;
}
