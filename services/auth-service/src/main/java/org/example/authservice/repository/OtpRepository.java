package org.example.authservice.repository;

import jakarta.transaction.Transactional;
import org.example.authservice.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpCode, Long> {
  Optional<OtpCode> findTopBySubjectIdAndPurposeAndActiveTrueOrderByCreatedAtDesc(
    Long subjectId,
    String purpose
  );


  @Modifying
  @Transactional
  @Query("UPDATE OtpCode o SET o.active = false " +
    "WHERE o.subjectId = :userId AND o.purpose = :purpose AND o.active = true")
  void deactivateActiveOtp(@Param("userId") Long userId,
                           @Param("purpose") String purpose);

}
