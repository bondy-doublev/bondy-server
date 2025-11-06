package org.example.notificationservice.service.interfaces;

import org.example.notificationservice.dto.request.CreateNotificationRequest;
import org.example.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface INotificationService {
  Page<Notification> getNotificationsByUserId(Long userId, Pageable pageable);

  Notification createNotification(CreateNotificationRequest request);

  void deleteNotification(Long userId, Long notificationId);

  void markRead(Long userId);
}
