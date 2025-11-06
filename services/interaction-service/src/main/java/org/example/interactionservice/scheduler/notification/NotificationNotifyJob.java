package org.example.interactionservice.scheduler.notification;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.commonweb.enums.NotificationType;
import org.example.commonweb.enums.RefType;
import org.example.interactionservice.client.AuthClient;
import org.example.interactionservice.client.NotificationClient;
import org.example.interactionservice.dto.request.CreateNotificationRequest;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Comment;
import org.example.interactionservice.entity.Reaction;
import org.example.interactionservice.repository.CommentRepository;
import org.example.interactionservice.repository.ReactionRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationNotifyJob {

  NotificationClient notificationClient;
  ReactionRepository reactionRepository;
  AuthClient authClient;

  CommentRepository commentRepository;

  @Scheduled(fixedDelay = 10_000)
  public void processReactionNotifications() {
    while (true) {
      List<Reaction> batch = reactionRepository.findUnnotifiedBatch();
      if (batch.isEmpty()) break;

      for (Reaction r : batch) {
        try {
          if (r.getPost().getUserId().equals(r.getUserId())) {
            reactionRepository.markAsNotified(List.of(r.getId()));
            continue;
          }

          UserBasicResponse actor = authClient.getBasicProfile(r.getUserId());
          if (actor == null) continue;

          HttpStatusCode status = notificationClient.notify(
            CreateNotificationRequest.builder()
              .userId(r.getPost().getUserId())
              .type(NotificationType.LIKE)
              .refType(RefType.POST)
              .refId(r.getPost().getId())
              .actorId(actor.getId())
              .actorName(actor.getFullName())
              .actorAvatarUrl(actor.getAvatarUrl())
              .build()
          );

          if (status.is2xxSuccessful() || status.value() == 409) {
            reactionRepository.markAsNotified(List.of(r.getId()));
          } else {
            log.warn("⚠️ Notification for reaction {} returned status {}", r.getId(), status);
          }

        } catch (Exception e) {
          log.error("❌ Failed to send notification for {}", r.getId(), e);
        }
      }
    }
  }

  @Scheduled(fixedDelay = 10_000)
  public void processCommentNotifications() {
    while (true) {
      List<Comment> batch = commentRepository.findUnnotifiedBatch();
      if (batch.isEmpty()) break;

      for (Comment c : batch) {
        try {
          if (c.getPost().getUserId().equals(c.getUserId())) {
            commentRepository.markAsNotified(List.of(c.getId()));
            continue;
          }

          UserBasicResponse actor = authClient.getBasicProfile(c.getUserId());

          if (actor == null) continue;

          HttpStatusCode status = notificationClient.notify(
            CreateNotificationRequest.builder()
              .userId(c.getPost().getUserId())
              .type(NotificationType.COMMENT)
              .refType(RefType.POST)
              .refId(c.getPost().getId())
              .actorId(actor.getId())
              .actorName(actor.getFullName())
              .actorAvatarUrl(actor.getAvatarUrl())
              .build()
          );

          if (status.is2xxSuccessful() || status.value() == 409) {
            commentRepository.markAsNotified(List.of(c.getId()));
          } else {
            log.warn("⚠️ Notification for comment {} returned status {}", c.getId(), status);
          }

        } catch (Exception e) {
          log.error("❌ Failed to send notification for {}", c.getId(), e);
        }
      }
    }
  }
}
