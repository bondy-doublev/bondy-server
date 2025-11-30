package org.example.moderationservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.moderationservice.config.security.ContextUser;
import org.example.moderationservice.dto.request.CreateReportRequest;
import org.example.moderationservice.entity.Report;
import org.example.moderationservice.service.ReportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportController {
  ReportService reportService;

  @PostMapping
  ApiResponse createReport(@RequestBody @Valid CreateReportRequest request) {
    Report report = reportService.createReport(ContextUser.get().getUserId(), request);

    return new ApiResponse();
  }
}
