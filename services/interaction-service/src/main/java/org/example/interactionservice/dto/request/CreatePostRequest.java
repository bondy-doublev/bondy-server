package org.example.interactionservice.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.validator.MaxSizeFromConfig.MaxSizeFromConfig;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePostRequest {
    String content;

    List<Long> tagUserIds;

    List<MultipartFile> mediaFiles;
}