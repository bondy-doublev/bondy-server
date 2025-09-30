package org.example.authservice.repository;

import org.example.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.avatarUrl = :avatarUrl WHERE u.id = :id")
    int updateAvatarUrlById(@Param("id") Long id, @Param("avatarUrl") String avatarUrl);
    Optional<User> findByEmail(String email);
}
