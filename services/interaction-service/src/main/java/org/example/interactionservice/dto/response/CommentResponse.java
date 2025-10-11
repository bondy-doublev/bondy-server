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
  Long postId;
  Long parentId;
  UserBasicResponse user;
  String contentText;
  Integer level;
  Long childCount;

  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}

