package org.example.interactionservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.enums.ReelVisibility;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReelResponse {
  Long id;
  UserBasicResponse owner;
  String videoUrl;
  ReelVisibility visibilityType;
  LocalDateTime expiresAt;
  Long viewCount;
  Boolean visible; // Kết quả đã qua logic kiểm tra
  List<Long> customAllowedUserIds;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}