package org.example.authservice.repository;

import org.example.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  @Modifying
  @Transactional
  @Query("""
    update RefreshToken r
    set r.revoked = true, r.revokedAt = :revokedAt
    where r.user.id = :userId and r.revoked = false
    """)
  void revokeTokens(@Param("userId") Long userId,
                    @Param("revokedAt") LocalDateTime revokedAt);

  @Query("""
    SELECT r.tokenHash
    FROM RefreshToken r
    WHERE r.user.id = :userId
      AND (r.revoked = false OR r.revoked IS NULL)
      AND r.expiresAt > CURRENT_TIMESTAMP
    """)
  Optional<String> findValidByUserId(@Param("userId") Long userId);

  Long userId(Long userId);
}
