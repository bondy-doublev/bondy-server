package org.example.communicationservice.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttachmentResponse {
  Long id;
  String url;
  String fileName;
  String mimeType;
  Long size;
  Integer width;
  Integer height;
}