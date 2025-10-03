package org.example.authservice.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateApiKeyRequest {
    private String name;
    private String prefix;
    private String rawKey;
    private LocalDateTime expiresAt;
}
