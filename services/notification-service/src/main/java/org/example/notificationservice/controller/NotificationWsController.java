// src/main/java/org/example/notificationservice/ws/NotificationWsController.java
package org.example.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.entity.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class NotificationWsController {
  private final SimpMessagingTemplate messagingTemplate;

  public void sendNotificationToUser(Long userId, Notification payload) {
    messagingTemplate.convertAndSendToUser(
      String.valueOf(userId),
      "/queue/notifications",
      payload
    );
  }

  public void publishPostEvent(Long postId, Object payload) {
    messagingTemplate.convertAndSend("/topic/post." + postId, payload);
  }
}
