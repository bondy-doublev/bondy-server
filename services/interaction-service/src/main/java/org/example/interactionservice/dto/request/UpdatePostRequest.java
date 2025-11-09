package org.example.interactionservice.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePostRequest {

  /**
   * Nội dung mới; null = không đổi. Có thể rỗng để xoá text.
   */
  @Size(max = 2000, message = "Content must be at most 2000 characters")
  String content;

  /**
   * Public/Private; null = không đổi.
   */
  Boolean isPublic;

  /**
   * Chỉ xoá media; null/empty = không xoá gì.
   */
  List<@Positive(message = "Attachment ID must be positive") Long> removeAttachmentIds;

  /**
   * Danh sách tag mới; null = không đổi, empty = xoá hết tag.
   */
  List<@Positive(message = "User ID must be positive") Long> tagUserIds;
}
