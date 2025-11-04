package org.example.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebSocketAuthHeaderFilter implements GlobalFilter {

  private static final Logger log = LoggerFactory.getLogger(WebSocketAuthHeaderFilter.class);

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String upgradeHeader = request.getHeaders().getFirst("Upgrade");

    if (upgradeHeader != null && "websocket".equalsIgnoreCase(upgradeHeader)) {
      var query = request.getQueryParams();
      String token = query.getFirst("access_token");
      String apiKey = query.getFirst("x_api_key");

      if (token != null || apiKey != null) {
        log.debug("🧩 Injecting WS headers: token={} apiKey={}", token != null, apiKey != null);

        ServerHttpRequest mutated = request.mutate()
          .headers(h -> {
            if (token != null && !token.isBlank()) {
              h.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
            if (apiKey != null && !apiKey.isBlank()) {
              h.set("X-API-KEY", apiKey);
            }
          })
          .build();

        return chain.filter(exchange.mutate().request(mutated).build());
      }
    }

    return chain.filter(exchange);
  }
}