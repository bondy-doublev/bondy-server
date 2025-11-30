package org.example.moderationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.moderation.ReportStatus;
import org.example.commonweb.enums.moderation.TargetType;
import org.example.moderationservice.entity.Base.BaseEntityWithUpdate;
import org.hibernate.annotations.DynamicInsert;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "reports")
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseEntityWithUpdate {
  @Column(name = "reporter_id")
  Long reporterId;

  @Column(name = "handled_by")
  Long handledBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type")
  TargetType targetType;

  @Column(name = "target_id")
  Long targetId;

  String reason;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  ReportStatus status = ReportStatus.OPEN;
}
