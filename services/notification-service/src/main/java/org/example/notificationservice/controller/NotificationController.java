package org.example.notificationservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.notificationservice.config.security.ContextUser;
import org.example.notificationservice.dto.PageRequestDto;
import org.example.notificationservice.dto.request.CreateNotificationRequest;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.service.interfaces.INotificationService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
  INotificationService notificationService;

  @GetMapping("/me")
  public AppApiResponse getNotifications(@ModelAttribute @Valid PageRequestDto filter) {
    return new AppApiResponse(notificationService.getNotificationsByUserId(ContextUser.get().getUserId(), filter.toPageable()));
  }

  @PostMapping("/notify")
  public AppApiResponse createNotification(@Valid @RequestBody CreateNotificationRequest request) {
    Notification notification = notificationService.createNotification(request);
    return new AppApiResponse(notification);
  }

  @PostMapping("/mark-read")
  public AppApiResponse markAllAsRead() {
    Long userId = ContextUser.get().getUserId();
    notificationService.markRead(userId);
    return new AppApiResponse();
  }

}
