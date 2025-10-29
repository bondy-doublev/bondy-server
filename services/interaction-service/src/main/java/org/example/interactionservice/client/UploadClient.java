package org.example.interactionservice.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.interactionservice.property.PropsConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UploadClient {
  WebClient.Builder webClientBuilder;
  String gatewayUrl;
  String apiKeyHeader;
  String apiKeyValue;
  String environment;

  public UploadClient(PropsConfig props, WebClient.Builder webClientBuilder) {
    this.webClientBuilder = webClientBuilder;
    gatewayUrl = props.getGateway().getUrl();
    apiKeyHeader = props.getApiKey().getHeader();
    apiKeyValue = props.getApiKey().getInternal();
    environment = props.getEnvironment();
  }

  public String uploadLocal(MultipartFile file) {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", file.getResource());

    return Objects.requireNonNull(webClientBuilder.build()
      .post()
      .uri(gatewayUrl + "/api/v1/upload/local")
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .header(apiKeyHeader, apiKeyValue)
      .body(BodyInserters.fromMultipartData(builder.build()))
      .retrieve()
      .bodyToMono(new ParameterizedTypeReference<AppApiResponse<String>>() {
      })
      .block()).getData();
  }

  public List<String> uploadLocalMultiple(List<MultipartFile> files) {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();

    for (MultipartFile file : files) {
      builder.part("files", file.getResource())
        .filename(Objects.requireNonNull(file.getOriginalFilename()))
        .contentType(file.getContentType() != null
          ? MediaType.parseMediaType(file.getContentType())
          : MediaType.APPLICATION_OCTET_STREAM);
    }

    String addressUpload = environment.equals("Production") ? "local" : "cloudinary";

    AppApiResponse<List<String>> response = webClientBuilder.build()
      .post()
      .uri(gatewayUrl + "/api/v1/upload/" + addressUpload + "/multiple")
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .header(apiKeyHeader, apiKeyValue)
      .body(BodyInserters.fromMultipartData(builder.build()))
      .retrieve()
      .bodyToMono(new ParameterizedTypeReference<AppApiResponse<List<String>>>() {
      })
      .block();

    return response != null ? response.getData() : List.of();
  }

  @lombok.Data
  private static class AppApiResponse<T> {
    boolean success;
    int code;
    T data;
  }
}
