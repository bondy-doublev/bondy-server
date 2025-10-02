package org.example.interactionservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.entity.Base.BaseEntity;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "friendships")
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Friendship extends BaseEntity {
    @Column(name = "user_id")
    Long userId;

    @Column(name = "friend_id")
    Long friendId;
    String status;

    @Column(name = "requested_at")
    LocalDateTime requestedAt;

    @Column(name = "responded_at")
    LocalDateTime respondedAt;
}
