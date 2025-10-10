package org.example.interactionservice.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.property.PropsConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthClient {
  WebClient.Builder webClientBuilder;
  String gatewayUrl;
  String apiKeyHeader;
  String apiKeyValue;

  public AuthClient(PropsConfig props, WebClient.Builder webClientBuilder) {
    this.webClientBuilder = webClientBuilder;
    this.gatewayUrl = props.getGateway().getUrl();
    this.apiKeyHeader = props.getApiKey().getHeader();
    this.apiKeyValue = props.getApiKey().getInternal();
  }

  public List<UserBasicResponse> getBasicProfiles(List<Long> userIds) {
    Map<String, Object> body = new HashMap<>();
    body.put("userIds", userIds);

    return Objects.requireNonNull(webClientBuilder.build()
        .post()
        .uri(gatewayUrl + "/api/v1/users/basic-profiles")
        .header(apiKeyHeader, apiKeyValue)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<AppApiResponse<List<UserBasicResponse>>>() {
        })
        .block())
      .getData();

  }

  @lombok.Data
  private static class AppApiResponse<T> {
    boolean success;
    int code;
    T data;
  }
}
