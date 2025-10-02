package org.example.interactionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.entity.Base.BaseEntity;
import org.hibernate.annotations.DynamicInsert;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "reactions")
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Reaction extends BaseEntity {
    @Column(name = "user_id")
    Long userId;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @EqualsAndHashCode.Exclude
    Post post;
}
