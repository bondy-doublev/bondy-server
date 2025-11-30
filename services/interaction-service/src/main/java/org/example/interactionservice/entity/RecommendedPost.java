package org.example.interactionservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedPost {
  private Long id;
  private String content_text;
  private Double score;
}

