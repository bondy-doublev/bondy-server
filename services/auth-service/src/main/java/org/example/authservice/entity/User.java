package org.example.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.authservice.entity.Base.BaseEntityWithUpdate;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "users")
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntityWithUpdate {
    String email;

    @Column(name = "first_name")
    String firstName;

    @Column(name = "middle_name")
    String middleName;

    @Column(name = "last_name")
    String lastName;

    @Column(name = "avatar_url")
    String avatarUrl;

    LocalDateTime dob;
    Boolean gender;
    String role;
    Boolean active;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Account> accounts = new HashSet<>();

    public void addAccount(Account account) {
        if (accounts == null) {
            accounts = new HashSet<>();
        }
        accounts.add(account);
        account.setUser(this);
    }
}
