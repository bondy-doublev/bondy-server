-- Thêm 2 cột
ALTER TABLE friendships
    ADD COLUMN request_notified BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN response_notified BOOLEAN NOT NULL DEFAULT FALSE;

-- Index phục vụ background job quét theo status + flag
CREATE INDEX idx_friendships_request_notified
    ON friendships (status, request_notified);

CREATE INDEX idx_friendships_response_notified
    ON friendships (status, response_notified);
