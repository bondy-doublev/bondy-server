package org.example.interactionservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCommentRequest {
  @NotNull(message = "Post ID is required")
  Long postId;

  Long parentId;

  @NotBlank(message = "Content is required")
  String content;
}
