-- V7 -- Add reel_read_users table for per-user read markers

CREATE TABLE reel_read_users (
    id BIGSERIAL PRIMARY KEY,
    reel_id BIGINT NOT NULL REFERENCES reels(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_reel_read_user ON reel_read_users(reel_id, user_id);

-- Index để hỗ trợ truy vấn kiểm tra đã đọc
CREATE INDEX idx_reel_read_user_reel ON reel_read_users(reel_id);
CREATE INDEX idx_reel_read_user_user ON reel_read_users(user_id);