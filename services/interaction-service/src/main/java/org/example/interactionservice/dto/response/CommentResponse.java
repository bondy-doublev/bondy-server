package org.example.interactionservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
  Long id;
  Long parentId;
  Long userId;
  String contentText;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}

