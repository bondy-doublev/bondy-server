-- Add columns to messages for type, edited, deleted flags
ALTER TABLE messages
  ADD COLUMN IF NOT EXISTS type VARCHAR(16) NOT NULL DEFAULT 'TEXT',
  ADD COLUMN IF NOT EXISTS is_edited BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS edited_at TIMESTAMP NULL,
  ADD COLUMN IF NOT EXISTS edited_by BIGINT NULL,
  ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL,
  ADD COLUMN IF NOT EXISTS deleted_by BIGINT NULL;

-- Create attachments table
CREATE TABLE IF NOT EXISTS message_attachments (
  id BIGSERIAL PRIMARY KEY,
  message_id BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
  url TEXT NOT NULL,
  file_name TEXT,
  mime_type VARCHAR(255),
  file_size BIGINT,
  width INT,
  height INT,
  created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_message_attachments_message ON message_attachments(message_id);