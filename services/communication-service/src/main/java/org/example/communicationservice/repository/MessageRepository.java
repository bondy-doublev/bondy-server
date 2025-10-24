package org.example.communicationservice.repository;

import org.example.communicationservice.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
  Page<Message> findByConversation_IdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
}