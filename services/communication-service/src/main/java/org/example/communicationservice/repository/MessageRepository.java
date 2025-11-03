package org.example.communicationservice.repository;

import org.example.communicationservice.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

  @Query("""
      SELECT m
      FROM Message m
      LEFT JOIN FETCH m.conversation
      LEFT JOIN FETCH m.attachments
      WHERE m.id = :id
    """)
  Optional<Message> findDetailById(Long id);

  Page<Message> findByConversation_IdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

  @Query("""
        SELECT COUNT(m)
        FROM Message m
        WHERE m.conversation.id = :conversationId
          AND m.isDeleted = false
          AND m.senderId <> :userId
          AND ( :lastReadAt IS NULL OR m.createdAt > :lastReadAt )
    """)
  long countUnread(Long conversationId, Long userId, LocalDateTime lastReadAt);

  @Query("""
        SELECT COUNT(m)
        FROM Message m
        WHERE m.conversation.id = :conversationId
          AND m.isDeleted = false
          AND m.senderId <> :userId
    """)
  long countUnreadWhenNoReadAt(Long conversationId, Long userId);

  interface UnreadRow {
    Long getConversationId();

    Long getUnread();
  }

  @Query(value = """
      SELECT m.conversation_id AS conversationId, COUNT(*) AS unread
      FROM messages m
      JOIN conversation_participants cp
        ON cp.conversation_id = m.conversation_id
       AND cp.user_id = :userId
      WHERE m.is_deleted = FALSE
        AND m.sender_id <> :userId
        AND (cp.last_read_at IS NULL OR m.created_at > cp.last_read_at)
      GROUP BY m.conversation_id
    """, nativeQuery = true)
  List<UnreadRow> findUnreadByUser(Long userId);
}