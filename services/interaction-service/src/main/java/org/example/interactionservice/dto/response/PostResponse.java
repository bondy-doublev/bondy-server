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
public class PostResponse {
  Long id;
  UserBasicResponse owner;
  String contentText;
  Integer mediaCount;
  Boolean visibility;

  Long reactionCount;
  Long commentCount;
  Long shareCount;

  Boolean reacted;

  List<MediaAttachmentResponse> mediaAttachments;
  List<UserBasicResponse> taggedUsers;

  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}
