package org.example.notificationservice.property;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PropsConfig {
  String environment;
  final Notify notify = new Notify();

  @Data
  public static class Notify {
    private int cleanUpAt;
  }
}
