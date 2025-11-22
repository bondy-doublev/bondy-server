package org.example.interactionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reel_read_users",
  uniqueConstraints = @UniqueConstraint(name = "uq_reel_read_user", columnNames = {"reel_id", "user_id"}))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReelReadUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reel_id", nullable = false)
  Reel reel;

  @Column(name = "user_id", nullable = false)
  Long userId;

  @Column(name = "read_at", nullable = false)
  LocalDateTime readAt;
}