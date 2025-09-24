package org.example.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.entity.BaseEntity;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "accounts")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @EqualsAndHashCode.Exclude
    User user;

    String provider;

    @Column(name = "password_hash")
    String passwordHash;
}
