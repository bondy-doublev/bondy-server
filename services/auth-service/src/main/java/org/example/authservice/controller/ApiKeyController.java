package org.example.authservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.authservice.service.interfaces.IApiKeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Auth", description = "Check API Key")
@RestController
@RequestMapping("/auth/api-key")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiKeyController {
    IApiKeyService service;

    @Hidden
    @PostMapping("/check")
    public ResponseEntity<Boolean> check(@RequestBody Map<String,String> body) {
        String apiKey = body.get("apiKey");
        boolean valid = service.validate(apiKey);
        return ResponseEntity.ok(valid);
    }
}
