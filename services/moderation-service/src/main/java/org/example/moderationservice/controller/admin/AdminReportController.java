package org.example.moderationservice.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.moderationservice.dto.PageRequestDto;
import org.example.moderationservice.entity.Report;
import org.example.moderationservice.service.admin.AdminReportService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Report")
@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminReportController {
  AdminReportService reportService;

  @GetMapping
  AppApiResponse getReports(@ModelAttribute @Valid PageRequestDto filter) {
    Page<Report> reports = reportService.getAllReports(filter.toPageable());

    return new AppApiResponse(reports.getContent());
  }
}
