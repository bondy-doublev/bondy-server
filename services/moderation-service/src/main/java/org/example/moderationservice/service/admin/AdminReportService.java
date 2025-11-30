package org.example.moderationservice.service.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.moderationservice.entity.Report;
import org.example.moderationservice.repository.admin.AdminReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminReportService {
  AdminReportRepository reportRepository;

  public Page<Report> getAllReports(Pageable pageable) {
    return reportRepository.findAll(pageable);
  }
}
