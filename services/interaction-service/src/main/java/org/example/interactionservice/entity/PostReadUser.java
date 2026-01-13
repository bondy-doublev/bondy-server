package org.example.interactionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Entity
@Table(name = "post_read_users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostReadUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    Post post;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "read_at", nullable = false)
    LocalDateTime readAt;
}
