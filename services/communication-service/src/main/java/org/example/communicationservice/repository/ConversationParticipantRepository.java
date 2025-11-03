package org.example.communicationservice.repository;

import org.example.communicationservice.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
  Optional<ConversationParticipant> findByConversation_IdAndUserId(Long conversationId, Long userId);

  @Query("select cp.userId from ConversationParticipant cp where cp.conversation.id = :conversationId")
  List<Long> findUserIdsByConversationId(Long conversationId);
}