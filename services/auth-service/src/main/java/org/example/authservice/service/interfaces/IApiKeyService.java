package org.example.authservice.service.interfaces;

public interface IApiKeyService {
    boolean validate(String rawKey);
}
