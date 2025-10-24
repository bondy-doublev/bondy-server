package org.example.communicationservice.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class WsHttpHeaderHandshakeInterceptor implements HandshakeInterceptor {

  @Override
  public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                 @NonNull ServerHttpResponse response,
                                 @NonNull WebSocketHandler wsHandler,
                                 @NonNull Map<String, Object> attributes) {
    var headers = request.getHeaders();
    attributes.put("X-User-Id", headers.getFirst("X-User-Id"));
    attributes.put("X-User-Role", headers.getFirst("X-User-Role"));
    attributes.put("X-Email", headers.getFirst("X-Email"));
    return true;
  }

  @Override
  public void afterHandshake(@NonNull ServerHttpRequest request,
                             @NonNull ServerHttpResponse response,
                             @NonNull WebSocketHandler wsHandler,
                             Exception exception) {
    // no-op
  }
}