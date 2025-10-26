package org.example.communicationservice.repository;

import org.example.communicationservice.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

  @Query("""
      SELECT c FROM Conversation c
      JOIN c.participants p1
      JOIN c.participants p2
      WHERE c.type = 'PRIVATE'
        AND p1.userId = :userId1
        AND p2.userId = :userId2
    """)
  Optional<Conversation> findPrivateBetween(Long userId1, Long userId2);

  @Query(
    value = """
      SELECT
        c.id                                   AS conversation_id,
        c.type                                 AS conversation_type,
        CASE WHEN c.type = 'PRIVATE' THEN other.user_id ELSE NULL END AS receiver_id,
        m.id                                   AS last_message_id,
        m.sender_id                            AS last_message_sender_id,
        m.content                              AS last_message_content,
        m.type                                 AS last_message_type,
        m.created_at                           AS last_message_created_at
      FROM conversations c
      JOIN conversation_participants cp
        ON cp.conversation_id = c.id
      LEFT JOIN LATERAL (
        SELECT cp2.user_id
        FROM conversation_participants cp2
        WHERE cp2.conversation_id = c.id
          AND cp2.user_id <> :userId
        LIMIT 1
      ) other ON TRUE
      LEFT JOIN LATERAL (
        SELECT msg.id, msg.sender_id, msg.content, msg.type, msg.created_at
        FROM messages msg
        WHERE msg.conversation_id = c.id
          AND msg.is_deleted = FALSE
        ORDER BY msg.created_at DESC, msg.id DESC
        LIMIT 1
      ) m ON TRUE
      WHERE cp.user_id = :userId
      ORDER BY COALESCE(m.created_at, c.created_at) DESC, c.id DESC
      """,
    countQuery = """
      SELECT COUNT(*) FROM (
        SELECT c.id
        FROM conversations c
        JOIN conversation_participants cp
          ON cp.conversation_id = c.id
        WHERE cp.user_id = :userId
        GROUP BY c.id
      ) t
      """,
    nativeQuery = true
  )
  Page<ConversationWithLastMessageRow> findConversationsWithLastMessageByUserId(Long userId, Pageable pageable);

  interface ConversationWithLastMessageRow {
    Long getConversation_id();

    String getConversation_type();

    Long getReceiver_id();

    Long getLast_message_id();

    Long getLast_message_sender_id();

    String getLast_message_content();

    String getLast_message_type();

    LocalDateTime getLast_message_created_at();
  }
}