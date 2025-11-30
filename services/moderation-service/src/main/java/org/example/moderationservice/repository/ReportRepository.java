package org.example.moderationservice.repository;

import org.example.commonweb.enums.moderation.ReportStatus;
import org.example.commonweb.enums.moderation.TargetType;
import org.example.moderationservice.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ReportRepository extends JpaRepository<Report, Long> {

  boolean existsByReporterIdAndTargetTypeAndTargetIdAndStatusIn(
    Long reporterId,
    TargetType targetType,
    Long targetId,
    Collection<ReportStatus> statuses
  );
}
