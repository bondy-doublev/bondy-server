package org.example.communicationservice.repository;

import org.example.communicationservice.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

  @Query("""
      SELECT m
      FROM Message m
      LEFT JOIN FETCH m.conversation
      LEFT JOIN FETCH m.attachments
      WHERE m.id = :id
    """)
  Optional<Message> findDetailById(Long id);

  // Giữ method cũ nếu bạn có
  Page<Message> findByConversation_IdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
}