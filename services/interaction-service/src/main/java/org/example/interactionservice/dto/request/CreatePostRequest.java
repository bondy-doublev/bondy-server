package org.example.interactionservice.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePostRequest {
  String content;

  List<@Positive(message = "User ID must be positive") Long> tagUserIds;

  List<MultipartFile> mediaFiles;

  Boolean isPublic;
}