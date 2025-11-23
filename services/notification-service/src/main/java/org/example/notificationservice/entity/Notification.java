package org.example.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.NotificationType;
import org.example.commonweb.enums.RefType;
import org.example.notificationservice.entity.Base.BaseEntity;
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

  @Column(name = "actor_name", nullable = false)
  String actorName;

  @Column(name = "actor_avatar_url", nullable = false)
  String actorAvatarUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  NotificationType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "ref_type", nullable = false, length = 20)
  RefType refType;

  @Column(name = "ref_id", nullable = false)
  Long refId;

  @Column(name = "is_read", nullable = false)
  Boolean isRead = false;

  @Column(name = "redirect_id", nullable = true)
  Long redirectId;
}