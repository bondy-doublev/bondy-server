package org.example.gateway.security;

import org.example.gateway.property.PropsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class ApiKeyFilter implements GlobalFilter {

  private static final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);
  private final WebClient webClient;
  private final AntPathMatcher matcher = new AntPathMatcher();

  private final String headerName;
  private final String authServiceUrl;
  private final List<String> publicPaths;

  public ApiKeyFilter(PropsConfig gatewayProps, WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
    var apiKeyProps = gatewayProps.getApiKey();
    this.headerName = apiKeyProps.getHeader();
    this.authServiceUrl = apiKeyProps.getAuthUrl();
    this.publicPaths = Objects.requireNonNullElse(apiKeyProps.getPublicPaths(), List.of());
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();

    if ("OPTIONS".equalsIgnoreCase(String.valueOf(request.getMethod()))) {
      return chain.filter(exchange);
    }

    String upgradeHeader = request.getHeaders().getFirst("Upgrade");
    if (upgradeHeader != null && "websocket".equalsIgnoreCase(upgradeHeader)) {
      log.debug("Skipping API Key filter for WebSocket handshake: {}", request.getPath());
      return chain.filter(exchange);
    }

    if (isPublic(path)) {
      return chain.filter(
        exchange.mutate()
          .request(r -> r.headers(this::stripSensitiveHeaders))
          .build()
      );
    }

    String apiKey = request.getHeaders().getFirst(headerName);
//        System.out.println("API key header: " + request.getHeaders());
//        System.out.println("API key header name: " + headerName);
//        System.out.println("API key: " + apiKey);

    if (apiKey == null || apiKey.isBlank()) {
      return FilterUtil.unauthorized(exchange, "Missing API Key");
    }

    return webClient.post()
      .uri(authServiceUrl)
      .bodyValue(Map.of("apiKey", apiKey))
      .retrieve()
      .bodyToMono(Boolean.class)
      .flatMap(valid -> {
        if (!Boolean.TRUE.equals(valid)) {
          return FilterUtil.unauthorized(exchange, "Invalid API Key");
        }

        ServerHttpRequest mutated = request.mutate()
          .headers(h -> {
            h.add("X-Auth-By", "gateway-apikey");
          })
          .build();

        return chain.filter(exchange.mutate().request(mutated).build());
      })
      .onErrorResume(e -> FilterUtil.unauthorized(exchange, "Auth service error"));
  }

  private boolean isPublic(String path) {
    return publicPaths.stream().anyMatch(p -> matcher.match(p, path));
  }

  private void stripSensitiveHeaders(HttpHeaders h) {
    h.remove("X-User-Id");
    h.remove("X-User-Role");
    h.remove("X-Email");
    h.remove("X-Auth-By");
  }
}
