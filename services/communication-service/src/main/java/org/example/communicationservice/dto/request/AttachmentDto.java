package org.example.communicationservice.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttachmentDto {
  String url;        // bắt buộc
  String fileName;   // tuỳ chọn
  String mimeType;   // tuỳ chọn
  Long size;         // tuỳ chọn
  Integer width;     // tuỳ chọn (ảnh)
  Integer height;    // tuỳ chọn (ảnh)
}