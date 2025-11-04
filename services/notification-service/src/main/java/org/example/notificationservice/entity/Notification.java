package org.example.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.notificationservice.entity.Base.BaseEntity;
import org.example.notificationservice.enums.RefType;
import org.example.notificationservice.enums.Type;
import org.hibernate.annotations.DynamicInsert;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "notifications")
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {
  @Column(name = "user_id", nullable = false)
  Long userId;

  @Column(name = "actor_id", nullable = false)
  Long actorId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  Type type;

  @Enumerated(EnumType.STRING)
  @Column(name = "ref_type", nullable = false, length = 20)
  RefType refType;

  @Column(name = "ref_id", nullable = false)
  Long refId;

  @Column(nullable = false)
  String message;

  @Column(name = "is_read", nullable = false)
  Boolean isRead = false;
}