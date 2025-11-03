package org.example.communicationservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnreadSummaryResponse {
  List<UnreadConversationCountResponse> items;
  long total;
}