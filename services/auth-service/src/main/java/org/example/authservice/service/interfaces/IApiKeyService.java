package org.example.authservice.service.interfaces;

import org.example.authservice.entity.ApiKey;

import java.time.LocalDateTime;
import java.util.List;

public interface IApiKeyService {
    ApiKey create(String name, String rawKey, String prefix, LocalDateTime expiresAt);
    ApiKey update(Long id, String name, LocalDateTime expiresAt, Boolean active);
    void delete(Long id);
    List<ApiKey> getAll();
    boolean validate(String rawKey);
}
