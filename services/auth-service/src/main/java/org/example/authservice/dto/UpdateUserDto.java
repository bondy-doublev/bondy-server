package org.example.authservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.validator.nullornotblank.NullOrNotBlank;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserDto {
  @NullOrNotBlank(message = "First name must not be empty string")
  String firstName;

  @NullOrNotBlank(message = "Middle name must not be empty string")
  String middleName;

  String avatarUrl;

  @NullOrNotBlank(message = "Last name must not be empty string")
  String lastName;
  LocalDateTime dob;
  Boolean gender;
}
