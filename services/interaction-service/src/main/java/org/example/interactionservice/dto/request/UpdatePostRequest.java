package org.example.interactionservice.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePostRequest {

  /**
   * Nội dung mới; null = không đổi. Có thể rỗng "" để xoá text.
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

  /**
   * Thêm media mới; null/empty = không thêm.
   * <p>
   * Validation:
   * - Tối đa 20 file/lần cập nhật (có thể chỉnh theo config)
   * - Chỉ chấp nhận image/* hoặc video/*
   * - Ảnh <= 10MB, Video <= 200MB (ví dụ; có thể lấy từ PropsConfig)
   */
  @Size(max = 20, message = "You can upload at most {max} media files")
  List<MultipartFile> newMediaFiles;
}
