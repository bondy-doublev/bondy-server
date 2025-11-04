package org.example.notificationservice.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.example.notificationservice.DTO.request.CreateNotificationRequest;
import org.example.notificationservice.controller.NotificationWsController;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.repository.NotificationRepository;
import org.example.notificationservice.service.interfaces.INotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService implements INotificationService {
  NotificationRepository notificationRepository;
  NotificationWsController notificationWsController;

  @Override
  public Page<Notification> getNotificationsByUserId(Long userId, Pageable pageable) {
    return notificationRepository.getByUserId(userId, pageable);
  }

  @Override
  @Transactional
  public Notification createNotification(CreateNotificationRequest request) {
    if (request.getUserId().equals(request.getActorId())) {
      throw new AppException(ErrorCode.BAD_REQUEST, "User cannot notify themselves");
    }

    boolean exists = notificationRepository.existsByUserIdAndRefTypeAndRefIdAndActorId(
      request.getUserId(),
      request.getRefType(),
      request.getRefId(),
      request.getActorId()
    );

    if (exists) {
      throw new AppException(ErrorCode.DUPLICATE_RESOURCE, "Notification already exists");
    }

    Notification notification = Notification.builder()
      .userId(request.getUserId())
      .actorId(request.getActorId())
      .type(request.getType())
      .refId(request.getRefId())
      .refType(request.getRefType())
      .message(request.getMessage())
      .isRead(false)
      .build();

    Notification saved = notificationRepository.save(notification);

    notificationWsController.sendNotificationToUser(saved.getUserId(), saved);

    return saved;
  }

  @Override
  public void deleteNotification(Long userId, Long notificationId) {

  }

  @Override
  public void markRead(Long userId) {
    notificationRepository.markAllAsReadByUserId(userId);
  }
}
