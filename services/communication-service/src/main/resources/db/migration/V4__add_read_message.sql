-- Thêm cột lưu thời điểm cuối cùng user đã đọc trong cuộc trò chuyện
ALTER TABLE conversation_participants
ADD COLUMN IF NOT EXISTS last_read_at TIMESTAMP NULL;