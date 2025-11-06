package org.example.notificationservice.config.security;

import lombok.Getter;

public class ContextUser {
  private static final ThreadLocal<ContextUser> CONTEXT = new ThreadLocal<>();

  @Getter
  private final Long userId;
  @Getter
  private final String role;
  @Getter
  private final String email;

  private ContextUser(Long userId, String role, String email) {
    this.userId = userId;
    this.role = role;
    this.email = email;
  }

  public static void set(Long userId, String role, String email) {
    CONTEXT.set(new ContextUser(userId, role, email));
  }

  public static ContextUser get() {
    return CONTEXT.get();
  }

  public static void clear() {
    CONTEXT.remove();
  }
}

