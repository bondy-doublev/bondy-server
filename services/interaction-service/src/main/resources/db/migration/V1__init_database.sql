-- FRIENDSHIP
CREATE TABLE friendships (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT now(),
    responded_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_friendship_not_self CHECK (user_id <> friend_id),
    CONSTRAINT ck_friendship_status CHECK (status IN ('PENDING','ACCEPTED','REJECTED','BLOCKED')),
    CONSTRAINT uq_friendship_direct UNIQUE (user_id, friend_id)
);

-- Chặn trùng cặp đảo ngược (A,B) vs (B,A)
CREATE UNIQUE INDEX uq_friendship_pair ON friendships (
              LEAST(user_id, friend_id),
              GREATEST(user_id, friend_id)
);

-- FOLLOW
CREATE TABLE follows (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_follow_not_self CHECK (follower_id <> followee_id),
    CONSTRAINT uq_follow UNIQUE (follower_id, followee_id)
);

-- POSTS
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content_text TEXT NULL,
    media_count SMALLINT NOT NULL DEFAULT 0,
    visibility BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NULL
);

-- MEDIA ATTACHMENTS
CREATE TABLE media_attachments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    type TEXT NOT NULL,
    url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_media_posts
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT ck_media_type CHECK (type IN ('IMAGE','VIDEO','OTHER'))
);

-- REACTIONS (like)
CREATE TABLE reactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_reactions_posts
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT uq_reactions_user_post UNIQUE (user_id, post_id)
);

-- SHARES
CREATE TABLE shares (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_shares_posts
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- COMMENTS
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT NULL,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_comments_posts
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
);

CREATE TABLE mentions (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NULL,
    comment_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_mentions_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_mentions_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,

    CONSTRAINT ck_mentions_scope CHECK (
        (post_id IS NOT NULL AND comment_id IS NULL)
        OR (post_id IS NULL AND comment_id IS NOT NULL)
    )
);

CREATE INDEX idx_friendship_user   ON friendships(user_id);
CREATE INDEX idx_friendship_friend ON friendships(friend_id);

CREATE INDEX idx_follow_follower ON follows(follower_id);
CREATE INDEX idx_follow_followee ON follows(followee_id);

CREATE INDEX idx_posts_user ON posts(user_id);

CREATE INDEX idx_media_post ON media_attachments(post_id);

CREATE INDEX idx_reactions_post ON reactions(post_id);

CREATE INDEX idx_shares_post ON shares(post_id);

CREATE INDEX idx_comments_post   ON comments(post_id);
CREATE INDEX idx_comments_parent ON comments(parent_id);

CREATE INDEX idx_mentions_post ON mentions(post_id);
CREATE INDEX idx_mentions_comment ON mentions(comment_id);
CREATE INDEX idx_mentions_user ON mentions(user_id);
