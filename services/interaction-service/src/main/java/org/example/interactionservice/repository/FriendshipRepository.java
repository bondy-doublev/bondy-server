package org.example.interactionservice.repository;

import jakarta.transaction.Transactional;
import org.example.interactionservice.entity.Friendship;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

  // Kiểm tra có tồn tại mối quan hệ giữa 2 user chưa
  Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

  // Lấy các request pending gửi tới user
  List<Friendship> findByFriendIdAndStatus(Long friendId, Friendship.Status status);

  // Lấy danh sách bạn đã accept của user
  List<Friendship> findByUserIdAndStatus(Long userId, Friendship.Status status);

  @Query("""
    SELECT f FROM Friendship f
    WHERE (f.userId = :userId1 AND f.friendId = :userId2)
       OR (f.userId = :userId2 AND f.friendId = :userId1)
    """)
  Optional<Friendship> findBetweenUsers(Long userId1, Long userId2);

  @Query("""
    SELECT f FROM Friendship f
    WHERE f.status = org.example.interactionservice.entity.Friendship.Status.PENDING
      AND f.requestNotified = false
    ORDER BY f.createdAt
    """)
  List<Friendship> findPendingUnnotified(Pageable pageable);

  default List<Friendship> findPendingUnnotified(int limit) {
    return findPendingUnnotified(PageRequest.of(0, limit));
  }

  @Query("""
    SELECT f FROM Friendship f
    WHERE f.status IN (
       org.example.interactionservice.entity.Friendship.Status.ACCEPTED,
       org.example.interactionservice.entity.Friendship.Status.REJECTED
    )
      AND f.responseNotified = false
      AND f.respondedAt IS NOT NULL
    ORDER BY f.respondedAt
    """)
  List<Friendship> findResponseUnnotified(Pageable pageable);

  default List<Friendship> findResponseUnnotified(int limit) {
    return findResponseUnnotified(PageRequest.of(0, limit));
  }

  @Transactional
  @Modifying
  @Query("UPDATE Friendship f SET f.requestNotified = true WHERE f.id IN :ids")
  void markRequestNotified(List<Long> ids);

  @Transactional
  @Modifying
  @Query("UPDATE Friendship f SET f.responseNotified = true WHERE f.id IN :ids")
  void markResponseNotified(List<Long> ids);
}
