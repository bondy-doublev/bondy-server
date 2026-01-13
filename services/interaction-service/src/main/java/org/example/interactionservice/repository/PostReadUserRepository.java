package org.example.interactionservice.repository;

import jakarta.transaction.Transactional;
import org.example.interactionservice.entity.PostReadUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PostReadUserRepository extends JpaRepository<PostReadUser, Long> {

    @Query("select r.post.id from PostReadUser r where r.userId = :userId and r.post.id in :postIds")
    Set<Long> findPostIdsByUserIdAndPostIdIn(@Param("userId") Long userId,
                                             @Param("postIds") List<Long> postIds);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO post_read_users(user_id, post_id)
        SELECT :userId, unnest(:postIds)
        ON CONFLICT (user_id, post_id) DO NOTHING
        """, nativeQuery = true)
    void insertIgnoreBatch(@Param("userId") Long userId,
                           @Param("postIds") Long[] postIds);
}

