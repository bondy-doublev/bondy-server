package org.example.authservice.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateApiKeyRequest {
    private String name;
    private LocalDateTime expiresAt;
    private Boolean active;
}
