package org.example.communicationservice.config.websocket;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.communicationservice.config.security.ContextUser;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContextUserChannelInterceptor implements ChannelInterceptor {

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      String userIdStr = accessor.getFirstNativeHeader("X-User-Id");
      String role = accessor.getFirstNativeHeader("X-User-Role");
      String email = accessor.getFirstNativeHeader("X-Email");

      if (!StringUtils.hasText(userIdStr)) {
        log.warn("[WS] CONNECT missing X-User-Id. native={}", accessor.toNativeHeaderMap());
        throw new IllegalArgumentException("Unauthorized: missing X-User-Id");
      }
      accessor.setUser(() -> userIdStr);

      Map<String, Object> attrs = accessor.getSessionAttributes();
      if (attrs != null) {
        attrs.put("X-User-Id", userIdStr);
        if (StringUtils.hasText(role)) attrs.put("X-User-Role", role);
        if (StringUtils.hasText(email)) attrs.put("X-Email", email);
      }
      ContextUser.set(Long.parseLong(userIdStr), role, email);

    } else {
      // Hydrate lại cho SEND/SUBSCRIBE
      Principal p = accessor.getUser();
      String userIdStr = (p != null ? p.getName() : null);

      if (!StringUtils.hasText(userIdStr) && accessor.getSessionAttributes() != null) {
        Object v = accessor.getSessionAttributes().get("X-User-Id");
        if (v != null) {
          userIdStr = v.toString();
          String finalUserIdStr = userIdStr;
          accessor.setUser(() -> finalUserIdStr); // hồi phục Principal nếu mất
        }
      }

      String role = null, email = null;
      if (accessor.getSessionAttributes() != null) {
        Object r = accessor.getSessionAttributes().get("X-User-Role");
        Object e = accessor.getSessionAttributes().get("X-Email");
        role = r != null ? r.toString() : null;
        email = e != null ? e.toString() : null;
      }

      if (StringUtils.hasText(userIdStr)) {
        ContextUser.set(Long.parseLong(userIdStr), role, email);
      } else {
        log.warn("[WS] {} without Principal and session attrs. headers={}", accessor.getCommand(), accessor.toNativeHeaderMap());
      }
    }

    return message;
  }

  @Override
  public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
    ContextUser.clear();
  }
}