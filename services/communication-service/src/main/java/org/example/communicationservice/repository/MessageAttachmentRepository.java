package org.example.communicationservice.repository;

import org.example.communicationservice.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {
}