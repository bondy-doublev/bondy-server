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
import org.example.interactionservice.repository.FriendshipRepository;
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
  FriendshipRepository friendshipRepository;
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
              .redirectId(isPostMention ? m.getPost().getId() : m.getComment().getPost().getId())
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
   * Thông báo khi có lời mời kết bạn mới
   * userId (sender)  -> gửi request
   * friendId (receiver) -> nhận noti
   */
  @Scheduled(fixedDelayString = "#{@propsConfig.notify.pollDelay}")
  public void processFriendRequestNotifications() {
    processBatch(
      "friend_request",
      () -> friendshipRepository.findPendingUnnotified(batchSize()),
      f -> {
        try {
          if (f.getUserId().equals(f.getFriendId())) {
            friendshipRepository.markRequestNotified(List.of(f.getId()));
            return;
          }

          UserBasicResponse actor = authClient.getBasicProfile(f.getUserId());
          if (actor == null) return;

          HttpStatusCode status = notificationClient.notify(
            CreateNotificationRequest.builder()
              .userId(f.getFriendId())
              .type(NotificationType.FRIEND_REQUEST)
              .refType(RefType.USER)
              .refId(f.getUserId())
              .actorId(actor.getId())
              .actorName(actor.getFullName())
              .actorAvatarUrl(actor.getAvatarUrl())
              .redirectId(f.getUserId())
              .build()
          );

          if (status.is2xxSuccessful() || status.value() == 409) {
            friendshipRepository.markRequestNotified(List.of(f.getId()));
          } else {
            log.warn("⚠️ Notification for friend request {} returned {}", f.getId(), status);
          }
        } catch (Exception e) {
          log.error("❌ Failed to send friend request notification for {}", f.getId(), e);
        }
      }
    );
  }

  /**
   * Thông báo khi lời mời kết bạn được ACCEPT / REJECT
   * friendId (receiver trước đó) -> là người hành động
   * userId (sender ban đầu)      -> là người nhận noti
   */
  @Scheduled(fixedDelayString = "#{@propsConfig.notify.pollDelay}")
  public void processFriendResponseNotifications() {
    processBatch(
      "friend_response",
      () -> friendshipRepository.findResponseUnnotified(batchSize()),
      f -> {
        try {
          if (f.getRespondedAt() == null) {
            return;
          }

          UserBasicResponse actor = authClient.getBasicProfile(f.getFriendId());
          if (actor == null) return;

          HttpStatusCode status = notificationClient.notify(
            CreateNotificationRequest.builder()
              .userId(f.getUserId())
              .type(NotificationType.FRIEND_ACCEPT)
              .refType(RefType.USER)
              .refId(f.getFriendId())
              .actorId(actor.getId())
              .actorName(actor.getFullName())
              .actorAvatarUrl(actor.getAvatarUrl())
              .redirectId(f.getFriendId())
              .build()
          );

          if (status.is2xxSuccessful() || status.value() == 409) {
            friendshipRepository.markResponseNotified(List.of(f.getId()));
          } else {
            log.warn("⚠️ Notification for friend response {} returned {}", f.getId(), status);
          }
        } catch (Exception e) {
          log.error("❌ Failed to send friend response notification for {}", f.getId(), e);
        }
      }
    );
  }

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

