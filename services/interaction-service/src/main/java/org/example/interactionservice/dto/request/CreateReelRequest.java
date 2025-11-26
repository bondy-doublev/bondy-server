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
public class CreateReelRequest {
  Long userId;                // Chủ sở hữu reel
  String videoUrl;            // FE upload trước rồi truyền URL vào
  ReelVisibility visibilityType;
  List<Long> customAllowedUserIds; // Dùng nếu visibilityType == CUSTOM
  Integer ttlHours;           // Tùy chọn override (default 24h)
}