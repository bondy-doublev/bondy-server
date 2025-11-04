package org.example.notificationservice.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.notificationservice.enums.RefType;
import org.example.notificationservice.enums.Type;

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

  @NotNull(message = "Reference ID must not be null")
  @Positive(message = "Reference ID must be a positive number")
  private Long refId;

  @NotNull(message = "Type must not be null")
  private Type type;

  @NotNull(message = "Reference type must not be null")
  private RefType refType;

  @NotBlank(message = "Message must not be blank")
  @Size(max = 255, message = "Message must not exceed 255 characters")
  private String message;
}
