package org.example.interactionservice.dto.response;

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
  String address;
  Integer friendCount;
}
