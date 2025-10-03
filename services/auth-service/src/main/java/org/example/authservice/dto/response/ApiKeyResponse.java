package org.example.authservice.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ApiKeyResponse {
    private Long id;
    private String name;
    private String prefix;
    private LocalDateTime expiresAt;
    private Boolean active;
}
