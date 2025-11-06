package org.example.interactionservice.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.interactionservice.dto.request.CreateNotificationRequest;
import org.example.interactionservice.property.PropsConfig;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationClient {
  WebClient.Builder webClientBuilder;
  String gatewayUrl;
  String apiKeyHeader;
  String apiKeyValue;

  public NotificationClient(PropsConfig props, WebClient.Builder webClientBuilder) {
    this.webClientBuilder = webClientBuilder;
    gatewayUrl = props.getGateway().getUrl();
    apiKeyHeader = props.getApiKey().getHeader();
    apiKeyValue = props.getApiKey().getInternal();
  }

  public HttpStatusCode notify(CreateNotificationRequest request) {
    return webClientBuilder.build()
      .post()
      .uri(gatewayUrl + "/api/v1/notifications/notify")
      .contentType(MediaType.APPLICATION_JSON)
      .header(apiKeyHeader, apiKeyValue)
      .bodyValue(request)
      .exchangeToMono(response -> Mono.just(response.statusCode()))
      .onErrorResume(ex -> {
        log.debug("NotificationClient Skip notify: {}", ex.getMessage());
        return Mono.empty();
      })
      .block();
  }

}
