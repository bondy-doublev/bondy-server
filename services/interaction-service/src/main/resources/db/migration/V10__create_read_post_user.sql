CREATE TABLE post_read_users (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_read_post
        FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,

    CONSTRAINT uq_read_post_user UNIQUE (user_id, post_id)
);

-- INDEXES
CREATE INDEX idx_post_read_users_user ON post_read_users(user_id);
CREATE INDEX idx_post_read_users_post ON post_read_users(post_id);
CREATE INDEX idx_post_read_users_read_at ON post_read_users(read_at);
CREATE INDEX idx_post_read_users_user_post ON post_read_users(user_id, post_id);
CREATE INDEX idx_post_read_users_user_read_at_desc ON post_read_users(user_id, read_at DESC);

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE post_vectors (
    post_id BIGINT PRIMARY KEY REFERENCES posts(id) ON DELETE CASCADE,
    content_vector VECTOR(500),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY,
    profile_vector VECTOR(500),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_post_vectors_hnsw ON post_vectors
    USING hnsw (content_vector vector_cosine_ops)
    WITH (m = 16, ef_construction = 200);
