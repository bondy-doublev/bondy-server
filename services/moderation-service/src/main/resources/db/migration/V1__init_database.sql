CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NULL,
    handle_by BIGINT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id   BIGINT NOT NULL,
    reason  TEXT NOT NULL,
    status  VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    severity SMALLINT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NULL,

    CONSTRAINT ck_reports_status CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','DISMISSED'))
);

CREATE TABLE admin_actions (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NULL,
    action_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id   BIGINT NOT NULL,
    reason   TEXT NOT NULL,
    metadata JSONB NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_reports_status ON reports(status);

CREATE INDEX idx_reports_target ON reports(target_type, target_id);

CREATE INDEX idx_reports_created_at ON reports(created_at DESC);

CREATE INDEX idx_reports_reporter ON reports(reporter_id);

CREATE INDEX idx_reports_handle_by ON reports(handle_by);

CREATE INDEX idx_adminactions_target ON admin_actions(target_type, target_id);

CREATE INDEX idx_adminactions_admin ON admin_actions(admin_id);

CREATE INDEX idx_adminactions_created_at ON admin_actions(created_at DESC);
