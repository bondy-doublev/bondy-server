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
public class PostDetailResponse {
  Long id;
  Long userId;
  String contentText;
  Integer mediaCount;
  Boolean visibility;

  Long reactionCount;
  Long commentCount;
  Long shareCount;

  List<MediaAttachmentResponse> mediaAttachments;
  List<CommentResponse> comments;

  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}
