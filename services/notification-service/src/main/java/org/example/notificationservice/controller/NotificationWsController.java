package org.example.notificationservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.notificationservice.service.NotificationService;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationWsController {

  NotificationService notificationService;

  @MessageMapping("/notification.markRead")
  public void markAllAsRead(Principal principal,
                            @Header("simpSessionAttributes") Map<String, Object> attrs) {
    Long userId = resolveUserId(principal, attrs);

    notificationService.markRead(userId);
  }

  private Long resolveUserId(Principal principal, Map<String, Object> attrs) {
    if (principal != null) {
      return Long.parseLong(principal.getName());
    } else if (attrs != null && attrs.get("X-User-Id") != null) {
      return Long.parseLong(attrs.get("X-User-Id").toString());
    }
    throw new IllegalArgumentException("Missing Principal or X-User-Id");
  }
}
