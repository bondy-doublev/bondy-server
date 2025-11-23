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
import org.example.interactionservice.property.PropsConfig;
import org.example.interactionservice.repository.CommentRepository;
import org.example.interactionservice.repository.MentionRepository;
import org.example.interactionservice.repository.ReactionRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationNotifyJob {

  NotificationClient notificationClient;
  ReactionRepository reactionRepository;
  CommentRepository commentRepository;
  MentionRepository mentionRepository;
  AuthClient authClient;
  PropsConfig propsConfig;

  // ✅ dùng chung config từ PropsConfig.Notify
  int batchSize() {
    return propsConfig.getNotify().getMaxBatch();
  }

  int maxLoops() {
    return propsConfig.getNotify().getMaxLoop();
  }

  /**
   * Xử lý thông báo reaction (like)
   */
  @Scheduled(fixedDelayString = "#{@propsConfig.notify.pollDelay}")
  public void processReactionNotifications() {
    processBatch(
      "reaction",
      () -> reactionRepository.findUnnotifiedBatch(batchSize()),
      r -> {
        if (r.getPost().getUserId().equals(r.getUserId())) {
          reactionRepository.markAsNotified(List.of(r.getId()));
          return;
        }

        UserBasicResponse actor = authClient.getBasicProfile(r.getUserId());
        if (actor == null) return;

        HttpStatusCode status = notificationClient.notify(
          CreateNotificationRequest.builder()
            .userId(r.getPost().getUserId())
            .type(NotificationType.LIKE)
            .refType(RefType.POST)
            .refId(r.getPost().getId())
            .actorId(actor.getId())
            .actorName(actor.getFullName())
            .actorAvatarUrl(actor.getAvatarUrl())
            .redirectId(r.getPost().getId())
            .build()
        );

        if (status.is2xxSuccessful() || status.value() == 409) {
          reactionRepository.markAsNotified(List.of(r.getId()));
        } else {
          log.warn("⚠️ Notification for reaction {} returned {}", r.getId(), status);
        }
      }
    );
  }

  /**
   * Xử lý thông báo comment
   */
  @Scheduled(fixedDelayString = "#{@propsConfig.notify.pollDelay}")
  public void processCommentNotifications() {
    processBatch(
      "comment",
      () -> commentRepository.findUnnotifiedBatch(batchSize()),
      c -> {
        if (c.getPost().getUserId().equals(c.getUserId())) {
          commentRepository.markAsNotified(List.of(c.getId()));
          return;
        }

        UserBasicResponse actor = authClient.getBasicProfile(c.getUserId());
        if (actor == null) return;

        HttpStatusCode status = notificationClient.notify(
          CreateNotificationRequest.builder()
            .userId(c.getPost().getUserId())
            .type(NotificationType.COMMENT)
            .refType(RefType.POST)
            .refId(c.getPost().getId())
            .actorId(actor.getId())
            .actorName(actor.getFullName())
            .actorAvatarUrl(actor.getAvatarUrl())
            .redirectId(c.getPost().getId())
            .build()
        );

        if (status.is2xxSuccessful() || status.value() == 409) {
          commentRepository.markAsNotified(List.of(c.getId()));
        } else {
          log.warn("⚠️ Notification for comment {} returned {}", c.getId(), status);
        }
      }
    );
  }

  /**
   * Xử lý thông báo Mention (tag người khác trong post hoặc comment)
   */
  @Scheduled(fixedDelayString = "#{@propsConfig.notify.pollDelay}")
  public void processMentionNotifications() {
    processBatch(
      "mention",
      () -> mentionRepository.findUnnotifiedBatch(batchSize()),
      m -> {
        try {
          boolean isPostMention = m.getPost() != null;
          Long targetUserId = isPostMention
            ? m.getPost().getUserId()
            : m.getComment().getUserId();

          if (targetUserId.equals(m.getUserId())) {
            mentionRepository.markAsNotified(List.of(m.getId()));
            return;
          }

          UserBasicResponse actor = authClient.getBasicProfile(targetUserId);
          if (actor == null) return;

          boolean isReplied = !isPostMention
            && m.getComment().getParent() != null
            && m.getComment().getParent().getUserId().equals(m.getUserId());

          HttpStatusCode status = notificationClient.notify(
            CreateNotificationRequest.builder()
              .userId(m.getUserId())
              .type(isReplied ? NotificationType.REPLY_COMMENT : NotificationType.MENTION)
              .refType(isPostMention ? RefType.POST : RefType.COMMENT)
              .refId(isPostMention ? m.getPost().getId() : m.getComment().getId())
              .actorId(actor.getId())
              .actorName(actor.getFullName())
              .actorAvatarUrl(actor.getAvatarUrl())
              .redirectId(m.getPost().getId())
              .build()
          );

          if (status.is2xxSuccessful() || status.value() == 409) {
            mentionRepository.markAsNotified(List.of(m.getId()));
          } else {
            log.warn("⚠️ Notification for mention {} returned {}", m.getId(), status);
          }
        } catch (Exception e) {
          log.error("❌ Failed to send mention notification for {}", m.getId(), e);
        }
      }
    );
  }

  /**
   * Hàm xử lý batch chung cho tất cả loại job
   */
  private <T> void processBatch(String name, Supplier<List<T>> fetch, Consumer<T> handler) {
    int loops = maxLoops();
    while (loops-- > 0) {
      List<T> batch = fetch.get();
      if (batch.isEmpty()) break;
      for (T item : batch) {
        try {
          handler.accept(item);
        } catch (Exception e) {
          log.error("❌ Error processing {} item: {}", name, item, e);
        }
      }
    }
  }
}

