package org.example.notificationservice.repository;

import jakarta.transaction.Transactional;
import org.example.commonweb.enums.NotificationType;
import org.example.commonweb.enums.RefType;
import org.example.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  boolean existsByUserIdAndRefTypeAndRefIdAndActorId(
    Long userId, RefType refType, Long refId, Long actorId);

  Page<Notification> getByUserId(Long userId, Pageable pageable);

  @Transactional
  @Modifying
  @Query("UPDATE Notification n SET n.isRead = TRUE WHERE n.userId = :userId AND n.isRead = FALSE")
  void markAllAsReadByUserId(Long userId);

  Optional<Notification> findByUserIdAndTypeAndRefTypeAndRefIdAndActorId(Long userId, NotificationType type, RefType refType, Long refId, Long actorId);

  @Transactional
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.createdAt < :threshold")
  void deleteOldNotifications(@Param("threshold") LocalDateTime threshold);
}
