package org.example.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.commonweb.DTO.core.ApiResponse;
import org.example.commonweb.DTO.core.ErrorResponse;
import org.example.commonweb.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class FilterUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
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
}
