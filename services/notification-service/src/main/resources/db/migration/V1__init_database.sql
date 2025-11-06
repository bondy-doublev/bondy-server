CREATE TABLE notifications
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT      NOT NULL,
    actor_id         BIGINT      NOT NULL,
    actor_name       TEXT        NOT NULL,
    actor_avatar_url TEXT        NOT NULL,
    type             VARCHAR(20) NOT NULL,
    ref_type         VARCHAR(20) NOT NULL,
    ref_id           BIGINT      NOT NULL,
    is_read          BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user ON notifications (user_id);
