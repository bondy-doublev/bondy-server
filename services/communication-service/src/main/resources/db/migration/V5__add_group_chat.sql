-- ========================
-- ALTER TABLE conversations
-- ========================
ALTER TABLE conversations
ADD COLUMN type VARCHAR(20) DEFAULT 'PRIVATE',
ADD COLUMN title VARCHAR(255) NULL,
ADD COLUMN avatar_url VARCHAR(255) NULL,
ADD COLUMN created_by BIGINT NULL;

-- ========================
-- ADD ROLE COLUMN TO PARTICIPANTS
-- ========================
ALTER TABLE conversation_participants
ADD COLUMN role VARCHAR(20) DEFAULT 'MEMBER';

-- ========================
-- OPTIONAL: INDEXES
-- ========================
CREATE INDEX idx_conversation_type ON conversations(type);
CREATE INDEX idx_conversation_participant_user ON conversation_participants(user_id);
