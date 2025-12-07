------------------------------------------------------------
-- 1. Thêm cột shared_from_post_id vào posts
------------------------------------------------------------

ALTER TABLE posts
    ADD COLUMN shared_from_post_id BIGINT NULL;

-- FK tự tham chiếu sang posts(id)
ALTER TABLE posts
    ADD CONSTRAINT fk_posts_shared_from_post
        FOREIGN KEY (shared_from_post_id) REFERENCES posts(id) ON DELETE SET NULL;

-- Index để query các bài share theo post gốc
CREATE INDEX idx_posts_shared_from
    ON posts(shared_from_post_id);


------------------------------------------------------------
-- 2. Chuyển dữ liệu từ bảng shares -> tạo Post share
------------------------------------------------------------
-- Nếu bạn có thêm cột reaction_count, comment_count, share_count
-- trong bảng posts, hãy set default tại đây luôn.

INSERT INTO posts (
    user_id,
    content_text,
    media_count,
    visibility,
    created_at,
    updated_at,
    shared_from_post_id
    -- , reaction_count, comment_count, share_count  -- nếu có
)
SELECT
    s.user_id,
    NULL AS content_text,       -- share không message
    0    AS media_count,
    TRUE AS visibility,
    s.created_at,
    NULL::timestamp AS updated_at,
    s.post_id AS shared_from_post_id
-- , 0 AS reaction_count, 0 AS comment_count, 0 AS share_count -- nếu có
FROM shares s;


------------------------------------------------------------
-- 3. (OPTIONAL) Cập nhật share_count cho bài gốc nếu có cột này
------------------------------------------------------------

-- Nếu bảng posts của bạn có cột share_count:
-- Tính lại share_count = số lượng post share từ post đó.

-- UPDATE posts p
-- SET share_count = COALESCE(sub.cnt, 0)
-- FROM (
--     SELECT shared_from_post_id AS post_id, COUNT(*) AS cnt
--     FROM posts
--     WHERE shared_from_post_id IS NOT NULL
--     GROUP BY shared_from_post_id
-- ) sub
-- WHERE p.id = sub.post_id;


------------------------------------------------------------
-- 4. Xoá bảng shares + index liên quan
------------------------------------------------------------

-- Xoá index nếu tồn tại (tuỳ tên index trong DB thực tế)
DROP INDEX IF EXISTS idx_shares_post;

DROP TABLE shares;
