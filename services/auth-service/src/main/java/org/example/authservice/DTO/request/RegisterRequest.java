package org.example.authservice.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "First name is required")
    String firstName;

    String middleName;

    @NotBlank(message = "Last name is required")
    String lastName;

    @NotNull(message = "Gender is required")
    Boolean gender;

    @NotNull(message = "Birthday is required")
    LocalDateTime dob;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 24, message = "Password must be between 8 and 24 characters")
    String password;
}

