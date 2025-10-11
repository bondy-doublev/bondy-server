package org.example.interactionservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCommentRequest {
  Long parentId;

  @NotBlank(message = "Content is required")
  String content;
}
