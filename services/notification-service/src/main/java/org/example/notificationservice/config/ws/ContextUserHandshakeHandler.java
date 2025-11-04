package org.example.notificationservice.config.ws;

import org.example.notificationservice.config.security.ContextUser;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class ContextUserHandshakeHandler extends DefaultHandshakeHandler {
  @Override
  protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
    var ctx = ContextUser.get();
    if (ctx != null && ctx.getUserId() != null) {
      return new UserPrincipal(ctx.getUserId());
    }
    var headers = request.getHeaders();
    var headerUserId = headers.getFirst("X-User-Id");
    if (headerUserId != null) {
      try {
        return new UserPrincipal(Long.parseLong(headerUserId));
      } catch (NumberFormatException ignored) {
      }
    }
    return null;
  }
}
