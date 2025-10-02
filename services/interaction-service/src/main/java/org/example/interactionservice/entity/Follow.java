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
@Table(name = "follows")
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Follow extends BaseEntity {
    @Column(name = "follower_id")
    Long followerId;

    @Column(name = "followee_id")
    Long followeeId;
}
