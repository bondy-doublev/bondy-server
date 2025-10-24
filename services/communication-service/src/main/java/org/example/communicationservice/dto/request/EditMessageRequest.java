package org.example.communicationservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EditMessageRequest {

  @NotNull
  Long messageId;

  // Chỉ hỗ trợ sửa nội dung text
  @NotNull
  @Size(min = 1, max = 5000)
  String content;
}