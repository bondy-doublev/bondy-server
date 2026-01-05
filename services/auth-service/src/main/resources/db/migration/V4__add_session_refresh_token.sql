ALTER TABLE refresh_tokens
    ADD COLUMN session_id VARCHAR(64);

DELETE FROM refresh_tokens
WHERE session_id IS NULL;

ALTER TABLE refresh_tokens
ALTER COLUMN session_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_refreshtokens_user_session
    ON refresh_tokens(user_id, session_id);