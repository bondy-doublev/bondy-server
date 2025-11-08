package org.example.interactionservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCommentRequest {
  Long parentId;

  @NotBlank(message = "Content is required")
  String content;

  List<@Positive(message = "User ID must be positive") Long> mentionUserIds;
}
