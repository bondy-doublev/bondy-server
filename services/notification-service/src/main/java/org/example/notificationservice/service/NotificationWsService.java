package org.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.entity.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationWsService {
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
