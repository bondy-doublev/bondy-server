package org.example.interactionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reel_allowed_users",
  uniqueConstraints = @UniqueConstraint(name = "uq_reel_allowed", columnNames = {"reel_id", "allowed_user_id"}))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReelAllowedUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reel_id", nullable = false)
  Reel reel;

  @Column(name = "allowed_user_id", nullable = false)
  Long allowedUserId;
}