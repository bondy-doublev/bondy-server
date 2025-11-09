package org.example.communicationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Conversation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  String type; // PRIVATE, GROUP

  @Column(name = "created_at")
  LocalDateTime createdAt;

  @Column(name = "title")
  String title; // tên nhóm

  @Column(name = "avatar_url")
  String avatarUrl;

  @Column(name = "created_by")
  Long createdBy;

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = LocalDateTime.now();
  }

  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @ToString.Exclude
  Set<ConversationParticipant> participants = new HashSet<>();
}