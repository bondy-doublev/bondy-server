package org.example.moderationservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.enums.moderation.ReportStatus;
import org.example.commonweb.exception.AppException;
import org.example.moderationservice.dto.request.CreateReportRequest;
import org.example.moderationservice.entity.Report;
import org.example.moderationservice.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportService {

  ReportRepository reportRepository;

  public Report createReport(Long reporterId, CreateReportRequest request) {
    boolean existsActive = reportRepository.existsByReporterIdAndTargetTypeAndTargetIdAndStatusIn(
      reporterId,
      request.getTargetType(),
      request.getTargetId(),
      List.of(ReportStatus.OPEN, ReportStatus.IN_PROGRESS)
    );

    if (existsActive) {
      throw new AppException(ErrorCode.DUPLICATE_RESOURCE, "Bạn đã báo cáo đối tượng này và chúng tôi đang xử lý.");
    }

    Report report = request.toEntity(reporterId);

    return reportRepository.save(report);
  }
}
