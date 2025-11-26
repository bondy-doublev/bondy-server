package org.example.interactionservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.enums.ReelVisibility;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateReelVisibilityRequest {
  Long reelId;
  Long requesterId;                // Người thực hiện
  ReelVisibility visibilityType;
  List<Long> customAllowedUserIds; // Nếu chuyển sang CUSTOM
}