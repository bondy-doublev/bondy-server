package org.example.notificationservice.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.example.notificationservice.dto.request.CreateNotificationRequest;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.repository.NotificationRepository;
import org.example.notificationservice.service.interfaces.INotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService implements INotificationService {
  NotificationRepository notificationRepository;
  NotificationWsService notificationWsService;

  @Override
  public Page<Notification> getNotificationsByUserId(Long userId, Pageable pageable) {
    return notificationRepository.getByUserId(userId, pageable);
  }

  @Override
  @Transactional
  public Notification createNotification(CreateNotificationRequest request) {
    if (Objects.equals(request.getUserId(), request.getActorId())) {
      throw new AppException(ErrorCode.BAD_REQUEST, "User cannot notify themselves");
    }

    boolean isUniqueType = switch (request.getType()) {
      case LIKE, FRIEND_REQUEST, FRIEND_ACCEPT -> true;
      default -> false;
    };

    Notification notification;

    if (isUniqueType) {
      Optional<Notification> existing = notificationRepository
        .findByUserIdAndTypeAndRefTypeAndRefIdAndActorId(
          request.getUserId(),
          request.getType(),
          request.getRefType(),
          request.getRefId(),
          request.getActorId()
        );

      if (existing.isPresent()) {
        notification = existing.get();
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
      } else {
        notification = buildNotification(request);
      }
    } else {
      notification = buildNotification(request);
    }

    Notification saved = notificationRepository.save(notification);

    notificationWsService.sendNotificationToUser(saved.getUserId(), saved);

    return saved;
  }

  @Override
  public void deleteNotification(Long userId, Long notificationId) {

  }

  @Override
  public void markRead(Long userId) {
    notificationRepository.markAllAsReadByUserId(userId);
  }

  private Notification buildNotification(CreateNotificationRequest req) {
    return Notification.builder()
      .userId(req.getUserId())
      .actorId(req.getActorId())
      .actorName(req.getActorName())
      .actorAvatarUrl(req.getActorAvatarUrl())
      .type(req.getType())
      .refType(req.getRefType())
      .refId(req.getRefId())
      .redirectId(req.getRedirectId())
      .isRead(false)
      .build();
  }
}
