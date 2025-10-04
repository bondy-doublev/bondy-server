package org.example.authservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.authservice.entity.User;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
  Long id;
  String email;
  String firstName;
  String middleName;
  String lastName;
  String avatarUrl;
  LocalDateTime dob;
  Boolean gender;

  public static UserResponse fromEntity(User user) {
    if (user == null) return null;

    return UserResponse.builder()
      .id(user.getId())
      .email(user.getEmail())
      .firstName(user.getFirstName())
      .middleName(user.getMiddleName())
      .lastName(user.getLastName())
      .avatarUrl(user.getAvatarUrl())
      .dob(user.getDob())
      .gender(user.getGender())
      .build();
  }
}
