package org.example.communicationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(
  name = "conversation_participants",
  uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "user_id"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationParticipant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "conversation_id", nullable = false)
  Conversation conversation;

  @Column(name = "user_id", nullable = false)
  Long userId;

  @Column(name = "last_read_at")
  LocalDateTime lastReadAt;

  @Column(name = "role")
  String role; // OWNER, ADMIN, MEMBER

  // ✅ Constructor tiện tạo nhanh participant
  public ConversationParticipant(Conversation conversation, Long userId, String role) {
    this.conversation = conversation;
    this.userId = userId;
    this.role = role;
    this.lastReadAt = LocalDateTime.now(); // cho có default an toàn
  }
}
