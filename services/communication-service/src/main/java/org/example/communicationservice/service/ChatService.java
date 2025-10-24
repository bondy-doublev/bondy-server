package org.example.communicationservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.communicationservice.entity.Conversation;
import org.example.communicationservice.entity.ConversationParticipant;
import org.example.communicationservice.entity.Message;
import org.example.communicationservice.repository.ConversationParticipantRepository;
import org.example.communicationservice.repository.ConversationRepository;
import org.example.communicationservice.repository.MessageRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {

  ConversationRepository conversationRepo;
  ConversationParticipantRepository participantRepo;
  MessageRepository messageRepo;

  @Transactional
  public Conversation getOrCreatePrivateConversation(Long userId1, Long userId2) {
    Optional<Conversation> existing = conversationRepo.findPrivateBetween(userId1, userId2);
    if (existing.isPresent()) return existing.get();

    Conversation c = new Conversation();
    c.setType("PRIVATE");
    c = conversationRepo.save(c);

    try {
      participantRepo.saveAll(List.of(
        ConversationParticipant.builder().conversation(c).userId(userId1).build(),
        ConversationParticipant.builder().conversation(c).userId(userId2).build()
      ));
    } catch (DataIntegrityViolationException e) {
      // rollback current tx và chạy query trong transaction mới
      return findExistingPrivateConversation(userId1, userId2);
    }

    return c;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  public Conversation findExistingPrivateConversation(Long userId1, Long userId2) {
    return conversationRepo.findPrivateBetween(userId1, userId2)
      .orElseThrow(() -> new IllegalStateException("Conversation not found after race condition"));
  }


  /**
   * Lưu tin nhắn mới vào cuộc trò chuyện.
   */
  @Transactional
  public Message saveMessage(Long conversationId, Long senderId, String content) {
    Conversation c = conversationRepo.findById(conversationId)
      .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

    Message m = Message.builder()
      .conversation(c)
      .senderId(senderId)
      .content(content)
      .build();

    return messageRepo.save(m);
  }

  /**
   * Lấy danh sách tin nhắn trong 1 cuộc trò chuyện (theo trang).
   */
  public Page<Message> getMessages(Long conversationId, int page, int size) {
    return messageRepo.findByConversation_IdOrderByCreatedAtDesc(
      conversationId, PageRequest.of(page, size)
    );
  }
}
