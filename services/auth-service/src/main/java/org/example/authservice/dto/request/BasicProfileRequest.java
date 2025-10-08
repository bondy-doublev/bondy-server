package org.example.authservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasicProfileRequest {

  @NotNull(message = "User IDs must not be null")
  @NotEmpty(message = "User IDs list must not be empty")
  List<@NotNull(message = "User ID cannot be null")
  @Positive(message = "User ID must be positive") Long> userIds;
}
