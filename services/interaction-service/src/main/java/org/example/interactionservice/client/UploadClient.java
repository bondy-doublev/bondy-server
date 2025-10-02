package org.example.interactionservice.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.property.PropsConfig;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UploadClient {
    WebClient.Builder webClientBuilder;
    String gatewayUrl;
    String apiKeyHeader;
    String apiKeyValue;

    public UploadClient(PropsConfig props, WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        gatewayUrl = props.getGateway().getUrl();
        apiKeyHeader = props.getApiKey().getHeader();
        apiKeyValue = props.getApiKey().getInternal();
    }

    public String uploadAvatar(MultipartFile file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());

        return webClientBuilder.build()
                .post()
                .uri(gatewayUrl + "/api/v1/upload/local")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header(apiKeyHeader, apiKeyValue)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
