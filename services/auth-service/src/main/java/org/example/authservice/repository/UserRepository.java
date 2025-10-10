package org.example.authservice.repository;

import org.example.authservice.dto.response.UserBasicResponse;
import org.example.authservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE User u SET u.avatarUrl = :avatarUrl WHERE u.id = :id")
  int updateAvatarUrlById(@Param("id") Long id, @Param("avatarUrl") String avatarUrl);

  Optional<User> findByEmail(String email);

  @Query("""
        SELECT new org.example.authservice.dto.response.UserBasicResponse(
            u.id,
            CONCAT(
                COALESCE(u.firstName, ''),
                CASE WHEN u.lastName IS NOT NULL AND u.lastName <> '' THEN CONCAT(' ', u.lastName) ELSE '' END,
                CASE WHEN u.middleName IS NOT NULL AND u.middleName <> '' THEN CONCAT(' ', u.middleName) ELSE '' END
            ),
            u.avatarUrl
        )
        FROM User u
        WHERE u.id IN :userIds
    """)
  List<UserBasicResponse> findBasicProfilesByIds(@Param("userIds") List<Long> userIds);

  @Query("""
        SELECT new org.example.authservice.dto.response.UserBasicResponse(
            u.id,
            CONCAT(
                COALESCE(u.firstName, ''),
                CASE WHEN u.lastName IS NOT NULL AND u.lastName <> '' THEN CONCAT(' ', u.lastName) ELSE '' END,
                CASE WHEN u.middleName IS NOT NULL AND u.middleName <> '' THEN CONCAT(' ', u.middleName) ELSE '' END
            ),
            u.avatarUrl
        )
        FROM User u
        WHERE u.id = :userId
    """)
  Optional<UserBasicResponse> findBasicProfileById(@Param("userId") Long userId);

  List<User> findByEmailContainingIgnoreCase(String email);

  @Query("""
        SELECT new org.example.authservice.dto.response.UserBasicResponse(
            u.id,
            CONCAT(
                COALESCE(u.firstName, ''),
                CASE WHEN u.lastName IS NOT NULL AND u.lastName <> '' THEN CONCAT(' ', u.lastName) ELSE '' END,
                CASE WHEN u.middleName IS NOT NULL AND u.middleName <> '' THEN CONCAT(' ', u.middleName) ELSE '' END
            ),
            u.avatarUrl
        )
        FROM User u
    """)
  Page<UserBasicResponse> findAllBasicProfiles(Pageable pageable);
}
