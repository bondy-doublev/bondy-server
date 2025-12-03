package org.example.interactionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.entity.Base.BaseEntity;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "friendships",
  uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "friend_id"})})
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Friendship extends BaseEntity {

  public enum Status {
    PENDING,
    ACCEPTED,
    REJECTED
  }

  @Column(name = "user_id", nullable = false)
  Long userId; // Người gửi request

  @Column(name = "friend_id", nullable = false)
  Long friendId; // Người nhận request

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  Status status;

  @Column(name = "requested_at", nullable = false)
  LocalDateTime requestedAt;

  @Column(name = "responded_at")
  LocalDateTime respondedAt;

  @Column(name = "request_notified", nullable = false)
  Boolean requestNotified;

  @Column(name = "response_notified", nullable = false)
  Boolean responseNotified;
}
