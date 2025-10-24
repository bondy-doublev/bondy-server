package org.example.communicationservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.communicationservice.dto.request.AttachmentDto;
import org.example.communicationservice.dto.request.EditMessageRequest;
import org.example.communicationservice.dto.request.SendMessageRequest;
import org.example.communicationservice.dto.response.AttachmentResponse;
import org.example.communicationservice.dto.response.ChatMessageResponse;
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

    // Service đã trả về entity đã fetch join đầy đủ
    Message saved = chatService.sendMessage(
      request.getConversationId(), senderId, MessageType.valueOf(request.getType()),
      request.getContent(), atts
    );

    ChatMessageResponse payload = toResponse(saved);
    String topic = "/topic/conversations." + saved.getConversation().getId();
    messagingTemplate.convertAndSend(topic, payload);
    messagingTemplate.convertAndSendToUser(String.valueOf(senderId), "/queue/messages", payload);
  }

  @MessageMapping("/chat.updateMessage")
  public void updateMessage(EditMessageRequest request,
                            Principal principal,
                            @Header("simpSessionAttributes") Map<String, Object> attrs) {
    Long editorId = resolveUserId(principal, attrs);
    Message updated = chatService.editOwnTextMessage(request.getMessageId(), editorId, request.getContent());

    ChatMessageResponse payload = toResponse(updated); // không còn LIE
    String topic = "/topic/conversations." + updated.getConversation().getId();
    messagingTemplate.convertAndSend(topic, payload);
  }

  @MessageMapping("/chat.deleteMessage")
  public void deleteMessage(Long messageId,
                            Principal principal,
                            @Header("simpSessionAttributes") Map<String, Object> attrs) {
    Long requesterId = resolveUserId(principal, attrs);
    Message deleted = chatService.softDeleteMessage(messageId, requesterId);

    ChatMessageResponse payload = toResponse(deleted); // không còn LIE
    String topic = "/topic/conversations." + deleted.getConversation().getId();
    messagingTemplate.convertAndSend(topic, payload);
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