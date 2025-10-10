package org.example.interactionservice.repository;

import org.example.interactionservice.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

  // Kiểm tra có tồn tại mối quan hệ giữa 2 user chưa
  Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

  // Lấy các request pending gửi tới user
  List<Friendship> findByFriendIdAndStatus(Long friendId, Friendship.Status status);

  // Lấy danh sách bạn đã accept của user
  List<Friendship> findByUserIdAndStatus(Long userId, Friendship.Status status);
}
