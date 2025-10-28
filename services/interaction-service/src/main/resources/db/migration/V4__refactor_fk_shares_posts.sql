ALTER TABLE shares
ALTER COLUMN post_id DROP NOT NULL;

ALTER TABLE shares
DROP CONSTRAINT IF EXISTS fk_shares_posts;

CREATE INDEX IF NOT EXISTS idx_shares_post ON shares(post_id);

ALTER TABLE shares
ADD CONSTRAINT fk_shares_posts
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE SET NULL;
