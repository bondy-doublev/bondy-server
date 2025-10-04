package org.example.authservice.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.authservice.dto.request.CreateApiKeyRequest;
import org.example.authservice.dto.request.UpdateApiKeyRequest;
import org.example.authservice.entity.ApiKey;
import org.example.authservice.service.interfaces.IApiKeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "API Key", description = "Manage API Keys")
@RestController
@RequestMapping("/auth/api-key")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiKeyController {

  IApiKeyService service;

  // CREATE API KEY
  @PostMapping("/create")
  public ResponseEntity<ApiKey> create(@RequestBody CreateApiKeyRequest body) {
    return ResponseEntity.ok(
      service.create(
        body.getName(),
        body.getRawKey(),
        body.getPrefix(),
        body.getExpiresAt()
      )
    );
  }

  // UPDATE API KEY
  @PutMapping("/update/{id}")
  public ResponseEntity<ApiKey> update(
    @Parameter(description = "ID of the API Key to update", required = true, example = "1")
    @PathVariable("id") Long id,
    @RequestBody UpdateApiKeyRequest body
  ) {
    return ResponseEntity.ok(
      service.update(
        id,
        body.getName(),
        body.getExpiresAt(),
        body.getActive()
      )
    );
  }


  // DELETE API KEY
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Void> delete(
    @Parameter(description = "ID of the API Key to delete", required = true, example = "1")
    @PathVariable("id") Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  // LIST ALL API KEYS
  @GetMapping("/list")
  public ResponseEntity<List<ApiKey>> list() {
    return ResponseEntity.ok(service.getAll());
  }

  // CHECK API KEY
  @PostMapping("/check")
  public ResponseEntity<Boolean> check(@RequestBody Map<String, String> body) {
    String apiKey = body.get("apiKey");
    return ResponseEntity.ok(service.validate(apiKey));
  }
}
