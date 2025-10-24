CREATE TABLE conversations (
  id BIGSERIAL PRIMARY KEY,
  type VARCHAR(32) NOT NULL, -- PRIVATE, GROUP (tương lai)
  created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE conversation_participants (
  id BIGSERIAL PRIMARY KEY,
  conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL,
  UNIQUE(conversation_id, user_id)
);

CREATE INDEX idx_participants_user ON conversation_participants(user_id);

CREATE TABLE messages (
  id BIGSERIAL PRIMARY KEY,
  conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  sender_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_messages_conversation ON messages(conversation_id);