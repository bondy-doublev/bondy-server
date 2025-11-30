package org.example.moderationservice.repository.admin;

import org.example.moderationservice.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminReportRepository extends JpaRepository<Report, Long> {
}
