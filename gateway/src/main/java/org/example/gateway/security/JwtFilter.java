package org.example.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.example.gateway.property.PropsConfig;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtFilter implements GlobalFilter {
    private final JwtService jwtService;
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final List<String> publicPaths;

    public JwtFilter(JwtService jwtService, PropsConfig props) {
        this.jwtService = jwtService;
        this.publicPaths = Objects.requireNonNullElse(props.getJwt().getPublicPaths(), List.of());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if ("OPTIONS".equalsIgnoreCase(String.valueOf(request.getMethod()))) {
            return chain.filter(exchange);
        }

        if (isPublic(path) && !path.equals("/api/v1/auth/refresh")) {
            return chain.filter(
                    exchange.mutate()
                            .request(r -> r.headers(this::stripSensitiveHeaders))
                            .build()
            );
        }

        String rawAuth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = JwtService.stripBearer(rawAuth);
        if (token == null) {
            return FilterUtil.unauthorized(exchange, "Missing Bearer token");
        }

        final Jws<Claims> jws;
        try {
            jws = jwtService.validate(token);
        } catch (JwtException e) {
            return FilterUtil.unauthorized(exchange, e.getMessage());
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
        h.remove("X-User-Role");
        h.remove("X-User-Roles");
        h.remove("X-Tenant");
        h.remove("X-Auth-By");
    }
}
