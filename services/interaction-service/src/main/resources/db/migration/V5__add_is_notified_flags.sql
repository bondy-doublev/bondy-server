-- Add is_notified flag to key tables and index for fast lookup

ALTER TABLE friendships
    ADD COLUMN is_notified BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_friendships_is_notified_false
    ON friendships(is_notified)
    WHERE is_notified = FALSE;

ALTER TABLE follows
    ADD COLUMN is_notified BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_follows_is_notified_false
    ON follows(is_notified)
    WHERE is_notified = FALSE;

ALTER TABLE posts
    ADD COLUMN is_notified BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_posts_is_notified_false
    ON posts(is_notified)
    WHERE is_notified = FALSE;

ALTER TABLE media_attachments
    ADD COLUMN is_notified BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_media_is_notified_false
    ON media_attachments(is_notified)
    WHERE is_notified = FALSE;

ALTER TABLE reactions
    ADD COLUMN is_notified BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_reactions_is_notified_false
    ON reactions(is_notified)
    WHERE is_notified = FALSE;

ALTER TABLE shares
    ADD COLUMN is_notified BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_shares_is_notified_false
    ON shares(is_notified)
    WHERE is_notified = FALSE;

ALTER TABLE comments
    ADD COLUMN is_notified BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_comments_is_notified_false
    ON comments(is_notified)
    WHERE is_notified = FALSE;

ALTER TABLE mentions
    ADD COLUMN is_notified BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_mentions_is_notified_false
    ON mentions(is_notified)
    WHERE is_notified = FALSE;
