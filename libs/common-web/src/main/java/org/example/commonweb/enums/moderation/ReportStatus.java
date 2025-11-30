package org.example.commonweb.enums.moderation;

public enum ReportStatus {

  // Mới tạo, chưa ai đụng
  OPEN,

  // Đã có admin nhận xử lý, đang review
  IN_PROGRESS,

  // Đã xử lý: report hợp lệ, đã áp dụng action
  RESOLVED,

  // Đã xử lý: report không hợp lệ / bị bỏ qua
  DISMISSED
}
