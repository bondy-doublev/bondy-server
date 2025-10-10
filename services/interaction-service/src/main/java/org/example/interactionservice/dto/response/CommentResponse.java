package org.example.interactionservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
  Long id;
  Long parentId;
  UserBasicResponse user;
  String contentText;

  List<CommentResponse> children;

  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}

