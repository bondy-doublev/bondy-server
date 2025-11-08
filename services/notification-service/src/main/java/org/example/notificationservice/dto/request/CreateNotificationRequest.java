package org.example.notificationservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

  @NotNull(message = "User ID must not be null")
  @Positive(message = "User ID must be a positive number")
  private Long userId;

  @NotNull(message = "Actor ID must not be null")
  @Positive(message = "Actor ID must be a positive number")
  private Long actorId;

  @NotBlank(message = "Actor name not null")
  private String actorName;

  private String actorAvatarUrl;

  @NotNull(message = "Reference ID must not be null")
  @Positive(message = "Reference ID must be a positive number")
  private Long refId;

  @NotNull(message = "Type must not be null")
  private NotificationType type;

  @NotNull(message = "Reference type must not be null")
  private RefType refType;
}
