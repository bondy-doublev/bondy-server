package org.example.communicationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Message {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "conversation_id")
  Conversation conversation;

  @Column(name = "sender_id")
  Long senderId;

  @Column(columnDefinition = "TEXT")
  String content;

  @Column(name = "created_at")
  LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = LocalDateTime.now();
  }
}