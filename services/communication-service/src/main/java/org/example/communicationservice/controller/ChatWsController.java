package org.example.communicationservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.communicationservice.dto.request.AttachmentDto;
import org.example.communicationservice.dto.request.EditMessageRequest;
import org.example.communicationservice.dto.request.SendMessageRequest;
import org.example.communicationservice.dto.response.*;
import org.example.communicationservice.entity.Message;
import org.example.communicationservice.entity.MessageAttachment;
import org.example.communicationservice.enums.MessageType;
import org.example.communicationservice.service.ChatService;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWsController {

  ChatService chatService;
  SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/chat.sendMessage")
  public void sendMessage(SendMessageRequest request,
                          Principal principal,
                          @Header("simpSessionAttributes") Map<String, Object> attrs) {
    Long senderId = resolveUserId(principal, attrs);

    List<MessageAttachment> atts = null;
    if (request.getAttachments() != null) {
      atts = request.getAttachments().stream().map(this::toEntity).toList();
    }

    Message saved = chatService.sendMessage(
      request.getConversationId(), senderId, MessageType.valueOf(request.getType()),
      request.getContent(), atts
    );

    ChatMessageResponse payload = toResponse(saved);
    Long conversationId = saved.getConversation().getId();
    String topic = "/topic/conversations." + conversationId;
    messagingTemplate.convertAndSend(topic, payload);
    messagingTemplate.convertAndSendToUser(String.valueOf(senderId), "/queue/messages", payload);

    // Notify unread cho các participant khác
    notifyUnreadForConversation(conversationId, senderId);
  }

  @MessageMapping("/chat.updateMessage")
  public void updateMessage(EditMessageRequest request,
                            Principal principal,
                            @Header("simpSessionAttributes") Map<String, Object> attrs) {
    Long editorId = resolveUserId(principal, attrs);
    Message updated = chatService.editOwnTextMessage(request.getMessageId(), editorId, request.getContent());

    ChatMessageResponse payload = toResponse(updated);
    String topic = "/topic/conversations." + updated.getConversation().getId();
    messagingTemplate.convertAndSend(topic, payload);
  }

  @MessageMapping("/chat.deleteMessage")
  public void deleteMessage(Long messageId,
                            Principal principal,
                            @Header("simpSessionAttributes") Map<String, Object> attrs) {
    Long requesterId = resolveUserId(principal, attrs);
    Message deleted = chatService.softDeleteMessage(messageId, requesterId);

    ChatMessageResponse payload = toResponse(deleted);
    String topic = "/topic/conversations." + deleted.getConversation().getId();
    messagingTemplate.convertAndSend(topic, payload);
  }

  // NEW: đánh dấu đã đọc qua WS
  @MessageMapping("/chat.readAll")
  public void readAll(Long conversationId,
                      Principal principal,
                      @Header("simpSessionAttributes") Map<String, Object> attrs) {
    Long userId = resolveUserId(principal, attrs);
    var readAt = chatService.markAllAsRead(conversationId, userId);

    var receipt = ReadReceiptResponse.builder()
      .conversationId(conversationId)
      .readerId(userId)
      .readAt(readAt)
      .build();

    // Broadcast read receipt
    String topic = "/topic/conversations." + conversationId + ".read";
    messagingTemplate.convertAndSend(topic, receipt);

    // Gửi cập nhật unread cho chính user
    long unread = chatService.getUnreadCount(conversationId, userId);
    var perConv = UnreadConversationCountResponse.builder()
      .conversationId(conversationId).unreadCount(unread).build();
    messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/unread.conversation", perConv);

    UnreadSummaryResponse summary = chatService.getUnreadSummary(userId);
    messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/unread.summary", summary);
  }

  // NEW: client yêu cầu snapshot tổng unread
  @MessageMapping("/chat.getUnread")
  public void getUnread(Principal principal,
                        @Header("simpSessionAttributes") Map<String, Object> attrs) {
    Long userId = resolveUserId(principal, attrs);
    UnreadSummaryResponse summary = chatService.getUnreadSummary(userId);
    System.out.println("[chat.getUnread] userId=" + userId);
    messagingTemplate.convertAndSendToUser(String.valueOf(userId),
      "/queue/unread.summary", "{}");
    messagingTemplate.convertAndSend("/user/" + userId + "/queue/unread.summary", summary);
    System.out.println("[chat.getUnread] sent trigger to /user/" + userId + "/queue/unread.summary");
  }

  private void notifyUnreadForConversation(Long conversationId, Long excludeUserId) {
    List<Long> participantIds = chatService.getParticipantUserIds(conversationId);
    System.out.println("[notifyUnreadForConversation] conversationId=" + conversationId
      + " excludeUserId=" + excludeUserId
      + " participants=" + participantIds);

    for (Long uid : participantIds) {
      if (excludeUserId != null && excludeUserId.equals(uid)) {
        System.out.println("  ➤ Skip sender: " + uid);
//        continue;
      }

      try {
//        long unread = chatService.getUnreadCount(conversationId, uid);
//        System.out.println("  ➤ Sending unread update to user " + uid + " (unread=" + unread + ")");
//
//        var perConv = UnreadConversationCountResponse.builder()
//          .conversationId(conversationId).unreadCount(unread).build();
//        messagingTemplate.convertAndSendToUser(String.valueOf(uid), "/queue/unread.conversation", perConv);

        UnreadSummaryResponse summary = chatService.getUnreadSummary(uid);
        System.out.println("Sending to user " + uid + " via /queue/unread.summary");
        System.out.println("Summary" + summary.getTotal());
        try {
          messagingTemplate.convertAndSendToUser(String.valueOf(uid), "/queue/unread.summary",
            summary);
        } catch (Exception e) {
          System.out.println("[notifyUnreadForConversation] " + e.getMessage());
        }
      } catch (Exception e) {
        System.err.println("  ⚠️ Error sending unread update to user " + uid + ": " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  private Long resolveUserId(Principal principal, Map<String, Object> attrs) {
    if (principal != null) {
      return Long.parseLong(principal.getName());
    } else if (attrs != null && attrs.get("X-User-Id") != null) {
      return Long.parseLong(attrs.get("X-User-Id").toString());
    }
    throw new IllegalArgumentException("Missing Principal on WS session");
  }

  private MessageAttachment toEntity(AttachmentDto dto) {
    return MessageAttachment.builder()
      .url(dto.getUrl())
      .fileName(dto.getFileName())
      .mimeType(dto.getMimeType())
      .size(dto.getSize())
      .width(dto.getWidth())
      .height(dto.getHeight())
      .build();
  }

  private ChatMessageResponse toResponse(Message m) {
    return ChatMessageResponse.builder()
      .id(m.getId())
      .conversationId(m.getConversation().getId())
      .senderId(m.getSenderId())
      .type(m.getType().name())
      .content(m.isDeleted() ? null : m.getContent())
      .attachments(m.getAttachments() == null ? List.of() :
        m.getAttachments().stream().map(a -> AttachmentResponse.builder()
          .id(a.getId())
          .url(a.getUrl())
          .fileName(a.getFileName())
          .mimeType(a.getMimeType())
          .size(a.getSize())
          .width(a.getWidth())
          .height(a.getHeight())
          .build()
        ).toList()
      )
      .createdAt(m.getCreatedAt())
      .edited(m.isEdited())
      .editedAt(m.getEditedAt())
      .deleted(m.isDeleted())
      .deletedAt(m.getDeletedAt())
      .build();
  }
}