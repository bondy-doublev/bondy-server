package org.example.notificationservice.config.ws;

import java.security.Principal;

public record UserPrincipal(Long userId) implements Principal {
  @Override
  public String getName() {
    return String.valueOf(userId);
  }
}
