package org.example.authservice.DTO.response;

import org.example.authservice.entity.RefreshToken;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthResponse {
    String accessToken;

    @JsonIgnore
    RefreshToken refreshToken;
}
