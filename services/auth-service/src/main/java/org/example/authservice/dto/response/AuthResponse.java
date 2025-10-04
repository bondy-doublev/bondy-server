package org.example.authservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.authservice.dto.RefreshTokenDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthResponse {
  String accessToken;
  UserResponse user;

  @JsonIgnore
  RefreshTokenDto refreshToken;
}
