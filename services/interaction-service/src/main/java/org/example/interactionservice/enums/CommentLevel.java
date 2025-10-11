package org.example.interactionservice.enums;

import lombok.Getter;

@Getter
public enum CommentLevel {
  LEVEL1(1),
  LEVEL2(2);

  private final int value;

  CommentLevel(int value) {
    this.value = value;
  }

}

