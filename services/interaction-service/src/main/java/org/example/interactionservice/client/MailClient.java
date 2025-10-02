package org.example.interactionservice.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.request.MailRequest;
import org.example.interactionservice.property.PropsConfig;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MailClient {
    WebClient.Builder webClientBuilder;
    String gatewayUrl;
    String apiKeyHeader;
    String apiKeyValue;

    public MailClient(PropsConfig props, WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        gatewayUrl = props.getGateway().getUrl();
        apiKeyHeader = props.getApiKey().getHeader();
        apiKeyValue = props.getApiKey().getInternal();
    }

    public void send(MailRequest request) {
        webClientBuilder.build()
                .post()
                .uri(gatewayUrl + "/api/v1/mail/send")
                .contentType(MediaType.APPLICATION_JSON)
                .header(apiKeyHeader, apiKeyValue)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
