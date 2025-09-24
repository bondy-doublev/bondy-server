package org.example.authservice.repository;

import org.example.authservice.entity.PreRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreRegRepository extends JpaRepository<PreRegistration, Long> {
    Optional<PreRegistration> findByEmail(String email);
}
