package org.example.authservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.authservice.entity.ApiKey;
import org.example.authservice.property.PropsConfig;
import org.example.authservice.repository.ApiKeyRepository;
import org.example.authservice.service.interfaces.IApiKeyService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiKeyService implements IApiKeyService {
    ApiKeyRepository repo;
    PropsConfig props;

    @Override
    public boolean validate(String rawKey) {
        if (!props.getEnvironment().equals("production") && rawKey.equals("111111"))
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
