package org.example.moderationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class PageRequestDto {

  @Min(value = 0, message = "Page index must be greater than or equal to 0")
  @Schema(example = "0", description = "Current page index (starting from 0)")
  private int page = 0;

  @Min(value = 1, message = "Page size must be greater than or equal to 1")
  @Schema(example = "10", description = "Number of items per page (must be >= 1)")
  private int size = 10;

  @Schema(example = "createdAt", description = "Field name to sort by (e.g., createdAt, name, id)")
  private String sortBy = "createdAt";

  @Schema(example = "desc", allowableValues = {"asc", "desc"},
    description = "Sort direction (asc or desc)")
  @Pattern(regexp = "asc|desc", flags = Pattern.Flag.CASE_INSENSITIVE,
    message = "Direction must be either 'asc' or 'desc'")
  private String direction = "desc";

  public Pageable toPageable() {
    if (sortBy == null || sortBy.isBlank()) {
      sortBy = "createdAt";
    }

    if (!sortBy.matches("(?i)createdAt|id|user_id|type|share_post_id")) {
      throw new IllegalArgumentException("Invalid sortBy value: " + sortBy);
    }

    Sort.Direction dir = Sort.Direction.fromString(direction);
    return PageRequest.of(page, size, Sort.by(dir, sortBy));
  }
}
