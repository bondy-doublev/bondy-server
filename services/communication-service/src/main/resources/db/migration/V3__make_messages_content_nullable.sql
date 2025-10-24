-- Cho phép content NULL để hỗ trợ IMAGE/FILE không có caption
ALTER TABLE messages
  ALTER COLUMN content DROP NOT NULL;