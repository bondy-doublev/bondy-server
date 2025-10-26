package org.example.communicationservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.communicationservice.dto.response.ConversationSummaryResponse;
import org.example.communicationservice.dto.response.LastMessageBriefResponse;
import org.example.communicationservice.entity.Conversation;
import org.example.communicationservice.entity.ConversationParticipant;
import org.example.communicationservice.entity.Message;
import org.example.communicationservice.entity.MessageAttachment;
import org.example.communicationservice.enums.MessageType;
import org.example.communicationservice.repository.ConversationParticipantRepository;
import org.example.communicationservice.repository.ConversationRepository;
import org.example.communicationservice.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    participantRepo.saveAll(List.of(
      ConversationParticipant.builder().conversation(c).userId(userId1).build(),
      ConversationParticipant.builder().conversation(c).userId(userId2).build()
    ));

    return c;
  }

  @Transactional
  public Message sendMessage(Long conversationId, Long senderId, MessageType type, String content, List<MessageAttachment> attachments) {
    Conversation c = conversationRepo.findById(conversationId)
      .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

    if (type == MessageType.TEXT) {
      if (content == null || content.isBlank()) {
        throw new IllegalArgumentException("content is required for TEXT message");
      }
    } else {
      if (attachments == null || attachments.isEmpty()) {
        throw new IllegalArgumentException("attachments is required for non-TEXT message");
      }
    }

    Message m = Message.builder()
      .conversation(c)
      .senderId(senderId)
      .type(type)
      .content(content)
      .build();

    if (attachments != null) {
      for (MessageAttachment a : attachments) {
        m.addAttachment(a);
      }
    }
    Message saved = messageRepo.save(m);
    return messageRepo.findDetailById(saved.getId())
      .orElseThrow(() -> new IllegalStateException("Message not found after save"));
  }

  @Transactional
  public Message editOwnTextMessage(Long messageId, Long editorId, String newContent) {
    Message m = messageRepo.findById(messageId)
      .orElseThrow(() -> new IllegalArgumentException("Message not found"));

    if (m.isDeleted()) {
      throw new IllegalStateException("Cannot edit deleted message");
    }
    if (m.getType() != MessageType.TEXT) {
      throw new IllegalArgumentException("Only TEXT messages can be edited");
    }
    if (!m.getSenderId().equals(editorId)) {
      throw new SecurityException("You can only edit your own message");
    }

    m.setContent(newContent);
    m.setEdited(true);
    m.setEditedAt(LocalDateTime.now());
    m.setEditedBy(editorId);
    messageRepo.save(m);

    return messageRepo.findDetailById(m.getId())
      .orElseThrow(() -> new IllegalStateException("Message not found after edit"));
  }

  @Transactional
  public Message softDeleteMessage(Long messageId, Long requesterId) {
    Message m = messageRepo.findById(messageId)
      .orElseThrow(() -> new IllegalArgumentException("Message not found"));

    if (m.isDeleted()) {
      return messageRepo.findDetailById(m.getId())
        .orElseThrow(() -> new IllegalStateException("Message not found after delete"));
    }

    if (!m.getSenderId().equals(requesterId)) {
      throw new SecurityException("You can only delete your own message");
    }

    m.setDeleted(true);
    m.setDeletedAt(LocalDateTime.now());
    m.setDeletedBy(requesterId);
    messageRepo.save(m);

    return messageRepo.findDetailById(m.getId())
      .orElseThrow(() -> new IllegalStateException("Message not found after delete"));
  }

  @Transactional(readOnly = true)
  public Page<Message> getMessages(Long conversationId, int page, int size) {
    return messageRepo.findByConversation_IdOrderByCreatedAtDesc(
      conversationId, PageRequest.of(page, size)
    );
  }

  @Transactional(readOnly = true)
  public Message getMessageDetail(Long id) {
    return messageRepo.findDetailById(id)
      .orElseThrow(() -> new IllegalArgumentException("Message not found"));
  }

  @Transactional(readOnly = true)
  public Page<ConversationSummaryResponse> getUserConversations(Long userId, int page, int size) {
    int pageSize = Math.max(size, 10);
    var pageable = PageRequest.of(page, pageSize);

    var rows = conversationRepo.findConversationsWithLastMessageByUserId(userId, pageable);

    return rows.map(r -> ConversationSummaryResponse.builder()
      .id(r.getConversation_id())
      .type(r.getConversation_type())
      .receiverId(r.getReceiver_id()) // NEW
      .lastMessage(
        r.getLast_message_id() == null ? null :
          LastMessageBriefResponse.builder()
            .id(r.getLast_message_id())
            .senderId(r.getLast_message_sender_id())
            .type(r.getLast_message_type())
            .content(r.getLast_message_content())
            .createdAt(r.getLast_message_created_at())
            .build()
      )
      .build()
    );
  }
}