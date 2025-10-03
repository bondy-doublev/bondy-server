package org.example.authservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.authservice.entity.ApiKey;
import org.example.authservice.property.PropsConfig;
import org.example.authservice.repository.ApiKeyRepository;
import org.example.authservice.service.interfaces.IApiKeyService;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiKeyService implements IApiKeyService {

    ApiKeyRepository repo;
    PropsConfig props;

    @Override
    public ApiKey create(String name, String rawKey, String prefix, LocalDateTime expiresAt) {
        String hash = hashKey(rawKey);
        ApiKey key = ApiKey.builder()
                .name(name)
                .prefix(prefix)
                .keyHash(hash)
                .expiresAt(expiresAt)
                .active(true)
                .build();
        return repo.save(key);
    }

    @Override
    public ApiKey update(Long id, String name, LocalDateTime expiresAt, Boolean active) {
        ApiKey key = repo.findById(id).orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "API key not found"));
        if(name != null) key.setName(name);
        if(expiresAt != null) key.setExpiresAt(expiresAt);
        if(active != null) key.setActive(active);
        return repo.save(key);
    }

    @Override
    public void delete(Long id) {
        ApiKey key = repo.findById(id).orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "API key not found"));
        repo.delete(key);
    }

    @Override
    public List<ApiKey> getAll() {
        return repo.findAll();
    }

    @Override
    public boolean validate(String rawKey) {
        // Cho dev env shortcut
        if (!props.getEnvironment().equals("production") && "111111".equals(rawKey))
            return true;

        String hash = hashKey(rawKey);
        return repo.findByKeyHash(hash)
                .filter(ApiKey::getActive)
                .filter(k -> k.getExpiresAt() == null
                        || k.getExpiresAt().isAfter(LocalDateTime.now(ZoneId.systemDefault())))
                .isPresent();
    }

    private String hashKey(String rawKey) {
        return DigestUtils.sha256Hex(rawKey);
    }
}
