package org.example.communicationservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.communicationservice.dto.request.SendMessageRequest;
import org.example.communicationservice.dto.response.ChatMessageResponse;
import org.example.communicationservice.entity.Message;
import org.example.communicationservice.service.ChatService;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
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

    Long senderId = null;
    if (principal != null) {
      senderId = Long.parseLong(principal.getName());
    } else if (attrs != null && attrs.get("X-User-Id") != null) {
      senderId = Long.parseLong(attrs.get("X-User-Id").toString());
    }
    if (senderId == null) {
      throw new IllegalArgumentException("Missing Principal on WS session");
    }

    Message saved = chatService.saveMessage(request.getConversationId(), senderId, request.getContent());

    ChatMessageResponse payload = ChatMessageResponse.builder()
      .id(saved.getId())
      .conversationId(saved.getConversation().getId())
      .senderId(saved.getSenderId())
      .content(saved.getContent())
      .createdAt(saved.getCreatedAt())
      .build();

    String topic = "/topic/conversations." + saved.getConversation().getId();
    messagingTemplate.convertAndSend(topic, payload);
    messagingTemplate.convertAndSendToUser(String.valueOf(senderId), "/queue/messages", payload);
  }
}