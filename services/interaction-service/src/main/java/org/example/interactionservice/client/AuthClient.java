package org.example.interactionservice.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.property.PropsConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
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

  public UserBasicResponse getBasicProfile(Long userId) {
    return Objects.requireNonNull(webClientBuilder.build()
        .get()
        .uri(gatewayUrl + "/api/v1/users/" + userId + "/basic-profile")
        .header(apiKeyHeader, apiKeyValue)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<AppApiResponse<UserBasicResponse>>() {
        })
        .block())
      .getData();
  }

  public List<UserBasicResponse> getAllBasicProfiles(int page) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder
      .fromUriString(gatewayUrl + "/api/v1/users/all-basic-profiles")
      .queryParam("page", page);

    AppApiResponse<List<UserBasicResponse>> response = webClientBuilder.build()
      .get()
      .uri(uriBuilder.toUriString())
      .header(apiKeyHeader, apiKeyValue)
      .retrieve()
      .bodyToMono(new ParameterizedTypeReference<AppApiResponse<List<UserBasicResponse>>>() {
      })
      .block();

    return Objects.requireNonNull(response).getData();
  }

  public void updateFriendCount(Long senderId, Long receiverId, String action) {
    if (!"add".equalsIgnoreCase(action) && !"remove".equalsIgnoreCase(action)) {
      throw new AppException(ErrorCode.BAD_REQUEST, "Invalid action: must be 'add' or 'remove'");
    }

    String url = String.format("%s/api/v1/users/friend-count?senderId=%d&receiverId=%d&action=%s",
      gatewayUrl, senderId, receiverId, action);

    log.info("Calling internal API: {}", url);

    webClientBuilder.build()
      .put()
      .uri(url)
      .header(apiKeyHeader, apiKeyValue)
      .retrieve()
      .toBodilessEntity()
      .block();
  }

  @lombok.Data
  private static class AppApiResponse<T> {
    boolean success;
    int code;
    T data;
  }
}
