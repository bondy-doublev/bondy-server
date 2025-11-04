CREATE TABLE notifications
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    actor_id   BIGINT      NOT NULL,
    type       VARCHAR(20) NOT NULL,
    ref_type   VARCHAR(20) NOT NULL,
    ref_id     BIGINT      NOT NULL,
    message    TEXT        NOT NULL,
    is_read    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user ON notifications (user_id);

ALTER TABLE notifications
    ADD CONSTRAINT unique_user_ref_actor UNIQUE (user_id, ref_type, ref_id, actor_id);
