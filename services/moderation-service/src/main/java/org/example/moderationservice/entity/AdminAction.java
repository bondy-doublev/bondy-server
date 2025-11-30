package org.example.moderationservice.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.moderation.ActionType;
import org.example.commonweb.enums.moderation.TargetType;
import org.example.moderationservice.entity.Base.BaseEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "admin_actions")
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class AdminAction extends BaseEntity {
  @Column(name = "admin_id")
  Long adminId;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_type")
  ActionType actionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type")
  TargetType targetType;

  @Column(name = "target_id")
  Long targetId;

  String reason;

  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb")
  Map<String, Object> metadata;
}
