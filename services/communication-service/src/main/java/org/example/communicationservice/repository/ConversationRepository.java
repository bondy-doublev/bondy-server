package org.example.communicationservice.repository;

import org.example.communicationservice.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
  @Query("""
      SELECT c FROM Conversation c
      JOIN c.participants p1
      JOIN c.participants p2
      WHERE c.type = 'PRIVATE'
        AND p1.userId = :userId1
        AND p2.userId = :userId2
    """)
  Optional<Conversation> findPrivateBetween(Long userId1, Long userId2);
}