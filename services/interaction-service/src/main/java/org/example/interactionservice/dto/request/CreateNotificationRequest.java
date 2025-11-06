package org.example.interactionservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.commonweb.enums.NotificationType;
import org.example.commonweb.enums.RefType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {
  private Long userId;

  private Long actorId;

  private String actorName;

  private String actorAvatarUrl;

  private Long refId;

  private NotificationType type;

  private RefType refType;
}
