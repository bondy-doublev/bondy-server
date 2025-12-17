package org.example.interactionservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePostRequest {
  @Size(max = 2000, message = "Content must be at most 2000 characters")
  String content;

  List<@Positive(message = "User ID must be positive") Long> tagUserIds;

  List<org.springframework.web.multipart.MultipartFile> mediaFiles;

  @NotNull(message = "isPublic is required")
  Boolean isPublic;
  Long originalPostId;
}
