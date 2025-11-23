-- V6 -- Create reels & reel_allowed_users tables

CREATE TABLE reels (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    video_url VARCHAR(1024) NOT NULL,
    visibility_type VARCHAR(20) NOT NULL, -- PUBLIC | PRIVATE | CUSTOM
    expires_at TIMESTAMP NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    view_count BIGINT NOT NULL DEFAULT 0,
    is_notified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tối ưu các truy vấn lấy reel còn sống (chưa hết hạn, chưa xóa)
CREATE INDEX idx_reels_alive ON reels(user_id, visibility_type)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_reels_expires_at ON reels(expires_at);

CREATE INDEX idx_reels_is_notified_false
    ON reels(is_notified)
    WHERE is_notified = FALSE;

-- Bảng mapping cho CUSTOM visibility
CREATE TABLE reel_allowed_users (
    id BIGSERIAL PRIMARY KEY,
    reel_id BIGINT NOT NULL REFERENCES reels(id) ON DELETE CASCADE,
    allowed_user_id BIGINT NOT NULL
);

CREATE UNIQUE INDEX uq_reel_allowed ON reel_allowed_users(reel_id, allowed_user_id);