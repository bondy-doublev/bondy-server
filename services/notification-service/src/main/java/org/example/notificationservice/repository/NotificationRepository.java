package org.example.notificationservice.repository;

import jakarta.transaction.Transactional;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.enums.RefType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  boolean existsByUserIdAndRefTypeAndRefIdAndActorId(
    Long userId, RefType refType, Long refId, Long actorId);

  Page<Notification> getByUserId(Long userId, Pageable pageable);

  @Transactional
  @Modifying
  @Query("UPDATE Notification n SET n.isRead = TRUE WHERE n.userId = :userId AND n.isRead = FALSE")
  void markAllAsReadByUserId(Long userId);
}
