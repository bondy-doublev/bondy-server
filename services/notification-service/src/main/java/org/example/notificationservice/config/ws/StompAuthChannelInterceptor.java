package org.example.notificationservice.config.ws;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.security.Principal;
import java.util.List;

public class StompAuthChannelInterceptor implements ChannelInterceptor {
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    var accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      Principal principal = accessor.getUser();
      if (principal == null) {
        // Thử lấy từ header STOMP (ví dụ client gửi trong connectHeaders)
        List<String> userIdHeaders = accessor.getNativeHeader("X-User-Id");
        if (userIdHeaders != null && !userIdHeaders.isEmpty()) {
          try {
            Long userId = Long.parseLong(userIdHeaders.get(0));
            principal = new UserPrincipal(userId);
            accessor.setUser(principal);
          } catch (NumberFormatException ignored) {
          }
        }
      }
      if (accessor.getUser() == null) {
        throw new IllegalArgumentException("Unauthorized: missing user principal");
      }
    }

    // Với SUBSCRIBE/SEND có thể kiểm tra thêm quyền truy cập nếu cần
    return message;
  }
}
