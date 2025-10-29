package org.example.authservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserBasicResponse {
  Long id;
  String fullName;
  String avatarUrl;
  Integer friendCount;
}
