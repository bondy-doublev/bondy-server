package org.example.communicationservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.communicationservice.config.security.ContextUser;
import org.example.communicationservice.dto.request.AttachmentDto;
import org.example.communicationservice.dto.request.EditMessageRequest;
import org.example.communicationservice.dto.request.SendMessageRequest;
import org.example.communicationservice.dto.response.AttachmentResponse;
import org.example.communicationservice.dto.response.ChatMessageResponse;
import org.example.communicationservice.dto.response.ConversationResponse;
import org.example.communicationservice.entity.Conversation;
import org.example.communicationservice.entity.Message;
import org.example.communicationservice.entity.MessageAttachment;
import org.example.communicationservice.enums.MessageType;
import org.example.communicationservice.service.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {

  ChatService chatService;
  SimpMessagingTemplate messagingTemplate;

  @PostMapping("/private/{otherUserId}")
  public AppApiResponse createOrGetPrivate(@PathVariable Long otherUserId) {
    Long selfId = ContextUser.get().getUserId();
    Conversation c = chatService.getOrCreatePrivateConversation(selfId, otherUserId);

    List<Long> participants = List.of(selfId, otherUserId);
    ConversationResponse res = ConversationResponse.builder()
      .id(c.getId())
      .type(c.getType())
      .participantIds(participants)
      .build();
    return new AppApiResponse(res);
  }

  @GetMapping("/history/{conversationId}")
  public AppApiResponse history(@PathVariable Long conversationId,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size) {
    Page<Message> p = chatService.getMessages(conversationId, page, size);
    List<ChatMessageResponse> data = p.getContent().stream().map(this::toResponse).toList();
    return new AppApiResponse(data);
  }

  @PostMapping("/messages")
  public AppApiResponse sendMessage(@RequestBody SendMessageRequest req) {
    Long selfId = ContextUser.get().getUserId();

    List<MessageAttachment> atts = null;
    if (req.getAttachments() != null) {
      atts = req.getAttachments().stream().map(this::toEntity).toList();
    }

    Message saved = chatService.sendMessage(
      req.getConversationId(), selfId, MessageType.valueOf(req.getType()), req.getContent(), atts
    );

    ChatMessageResponse payload = toResponse(saved);
    String topic = "/topic/conversations." + saved.getConversation().getId();
    messagingTemplate.convertAndSend(topic, payload);
    messagingTemplate.convertAndSendToUser(String.valueOf(selfId), "/queue/messages", payload);

    return new AppApiResponse(payload);
  }

  @PutMapping("/messages/{messageId}")
  public AppApiResponse editMessage(@PathVariable Long messageId,
                                    @RequestBody EditMessageRequest req) {
    Long selfId = ContextUser.get().getUserId();
    if (!messageId.equals(req.getMessageId())) {
      throw new IllegalArgumentException("messageId mismatch");
    }
    Message updated = chatService.editOwnTextMessage(messageId, selfId, req.getContent());

    ChatMessageResponse payload = toResponse(updated);
    String topic = "/topic/conversations." + updated.getConversation().getId();
    messagingTemplate.convertAndSend(topic, payload);

    return new AppApiResponse(payload);
  }

  @DeleteMapping("/messages/{messageId}")
  public AppApiResponse deleteMessage(@PathVariable Long messageId) {
    Long selfId = ContextUser.get().getUserId();
    Message deleted = chatService.softDeleteMessage(messageId, selfId);

    ChatMessageResponse payload = toResponse(deleted);
    String topic = "/topic/conversations." + deleted.getConversation().getId();
    messagingTemplate.convertAndSend(topic, payload);

    return new AppApiResponse(payload);
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