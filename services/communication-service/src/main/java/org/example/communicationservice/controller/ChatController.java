package org.example.communicationservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.communicationservice.config.security.ContextUser;
import org.example.communicationservice.dto.response.ChatMessageResponse;
import org.example.communicationservice.dto.response.ConversationResponse;
import org.example.communicationservice.entity.Conversation;
import org.example.communicationservice.entity.Message;
import org.example.communicationservice.service.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {

  ChatService chatService;

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
    List<ChatMessageResponse> data = p.getContent().stream().map(m -> ChatMessageResponse.builder()
      .id(m.getId())
      .conversationId(m.getConversation().getId())
      .senderId(m.getSenderId())
      .content(m.getContent())
      .createdAt(m.getCreatedAt())
      .build()).toList();
    return new AppApiResponse(data);
  }
}