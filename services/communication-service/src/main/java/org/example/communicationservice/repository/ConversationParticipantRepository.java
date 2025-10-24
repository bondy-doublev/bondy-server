package org.example.communicationservice.repository;

import org.example.communicationservice.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
}