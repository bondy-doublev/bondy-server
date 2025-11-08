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
  Long postId;
  Long parentId;
  List<UserBasicResponse> mentions;
  UserBasicResponse user;
  String contentText;
  Integer level;
  Long childCount;

  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}

