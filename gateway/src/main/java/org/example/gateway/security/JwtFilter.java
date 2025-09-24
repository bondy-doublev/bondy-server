package org.example.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.example.commonweb.DTO.core.ApiResponse;
import org.example.commonweb.DTO.core.ErrorResponse;
import org.example.commonweb.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtFilter implements GlobalFilter {
    private final JwtService jwtService;
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final List<String> publicPaths;

    public JwtFilter(
            JwtService jwtService,
            @Value("${gateway.public-paths:/auth/**,/actuator/**,/docs/**}") String publicPathsCsv
    ) {
        this.jwtService = jwtService;
        this.publicPaths = parseCsvOrList(publicPathsCsv);
    }

    private static List<String> parseCsvOrList(String v) {
        if (v == null || v.isBlank()) return List.of();
        String cleaned = v.replaceAll("[\\[\\]]", "");
        return Arrays.stream(cleaned.split("\\s*,\\s*"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod().toString())) {
            return chain.filter(exchange);
        }

        if (isPublic(path)) {
            return chain.filter(
                    exchange.mutate()
                            .request(r -> r.headers(this::stripSensitiveHeaders))
                            .build()
            );
        }

        String rawAuth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = JwtService.stripBearer(rawAuth);
        if (token == null) {
            return unauthorized(exchange, "Missing Bearer token");
        }

        final Jws<Claims> jws;
        try {
            jws = jwtService.validate(token);
        } catch (JwtException e) {
            return unauthorized(exchange, "Invalid token");
        }

        Claims claims = jws.getPayload();
        String userId = claims.getSubject();
        String role = claims.get("role", String.class);
        String email = claims.get("email", String.class);

        ServerHttpRequest mutated = request.mutate()
                .headers(h -> {
                    stripSensitiveHeaders(h);
                    if (userId != null) h.add("X-User-Id", userId);
                    if (role != null)  h.add("X-User-Role", role);
                    if (email != null) h.add("X-Email", email);
                    h.add("X-Auth-By", "gateway");
                })
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isPublic(String path) {
        return publicPaths.stream().anyMatch(p -> matcher.match(p, path));
    }

    private void stripSensitiveHeaders(HttpHeaders h) {
        h.remove("X-User-Id");
        h.remove("X-User-Roles");
        h.remove("X-Tenant");
        h.remove("X-Auth-By");
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse res = exchange.getResponse();
        res.setStatusCode(HttpStatus.UNAUTHORIZED);
        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ErrorCode ec = ErrorCode.UNAUTHORIZED;

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .code(ec.getCode())
                .data(ErrorResponse.builder()
                        .type(ec.name())
                        .message(msg)
                        .build())
                .build();

        byte[] body;
        try {
            body = MAPPER.writeValueAsBytes(response);
        } catch (Exception e) {
            body = ("{\"success\":false," +
                    "\"code\":401," +
                    "\"data\":{" +
                        "\"type\":\"UNAUTHORIZED\"," +
                        "\"message\":\"Unauthorized\"}" +
                    "}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        return res.writeWith(Mono.just(res.bufferFactory().wrap(body)));
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
