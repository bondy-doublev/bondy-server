package org.example.interactionservice.repository;

import org.example.interactionservice.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Post, Long> {

  @Query(
    value = """
      SELECT *
      FROM (
          SELECT 'POST' AS type, p.id, p.user_id, p.created_at AS createdAt, NULL AS share_post_id
          FROM posts p
          WHERE p.visibility = true
          UNION ALL
          SELECT 'SHARE' AS type, s.id, s.user_id, s.created_at AS createdAt, s.post_id AS share_post_id
          FROM shares s
      ) AS feed
      """,
    countQuery = """
      SELECT COUNT(*) FROM (
          SELECT id FROM posts WHERE visibility = true
          UNION ALL
          SELECT id FROM shares
      ) AS feed
      """,
    nativeQuery = true
  )
  Page<Object[]> getFeed(Pageable pageable);

  @Query(
    value = """
      SELECT *
      FROM (
          SELECT 'POST' AS type, p.id, p.user_id, p.created_at AS createdAt, NULL AS share_post_id, NULL AS owner_id
          FROM posts p
          WHERE p.visibility = true AND p.user_id = :userId
          UNION ALL
          SELECT 'SHARE' AS type, s.id, s.user_id, s.created_at AS createdAt, s.post_id AS share_post_id, p2.user_id AS owner_id 
          FROM shares s
          JOIN posts p2 ON p2.id = s.post_id
          WHERE s.user_id = :userId
      ) AS feed
      ORDER BY createdAt DESC
      """,
    countQuery = """
      SELECT COUNT(*) FROM (
          SELECT id
          FROM posts
          WHERE visibility = true AND user_id = :userId
          UNION ALL
          SELECT id
          FROM shares
          WHERE user_id = :userId
      ) AS feed
      """,
    nativeQuery = true
  )
  Page<Object[]> getWallFeed(@Param("userId") Long userId, Pageable pageable);
}
