package org.example.communicationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.communicationservice.enums.MessageType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
  @JoinColumn(name = "conversation_id", nullable = false)
  Conversation conversation;

  @Column(name = "sender_id", nullable = false)
  Long senderId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  MessageType type;

  // Với TEXT: content bắt buộc; IMAGE/FILE: content có thể null
  @Column(columnDefinition = "TEXT")
  String content;

  @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  List<MessageAttachment> attachments = new ArrayList<>();

  @Column(name = "created_at")
  LocalDateTime createdAt;

  // Edit flags
  @Column(name = "is_edited", nullable = false)
  @Builder.Default
  boolean isEdited = false;

  @Column(name = "edited_at")
  LocalDateTime editedAt;

  @Column(name = "edited_by")
  Long editedBy;

  // Soft delete flags
  @Column(name = "is_deleted", nullable = false)
  @Builder.Default
  boolean isDeleted = false;

  @Column(name = "deleted_at")
  LocalDateTime deletedAt;

  @Column(name = "deleted_by")
  Long deletedBy;

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = LocalDateTime.now();
  }

  // Helper
  public void addAttachment(MessageAttachment a) {
    a.setMessage(this);
    this.attachments.add(a);
  }
}