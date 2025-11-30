package org.example.moderationservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.moderation.ReportStatus;
import org.example.commonweb.enums.moderation.TargetType;
import org.example.moderationservice.entity.Report;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReportRequest {

  @NotNull
  TargetType targetType;

  @NotNull
  Long targetId;

  @NotBlank
  @Size(max = 2000)
  String reason;

  public Report toEntity(Long reporterId) {
    return Report.builder()
      .reporterId(reporterId)
      .targetType(targetType)
      .targetId(targetId)
      .reason(reason)
      .status(ReportStatus.OPEN)
      .build();
  }
}
