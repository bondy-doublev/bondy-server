package org.example.authservice.property;

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
  final Gateway gateway = new Gateway();
  final ApiKey apiKey = new ApiKey();
  final User user = new User();
  final Otp otp = new Otp();
  final Jwt jwt = new Jwt();

  @Data
  public static class ApiKey {
    private String header;
    private String internal;
  }

  @Data
  public static class Gateway {
    private String url;
  }

  @Data
  public static class User {
    private String defaultPasswordSuffix;
  }

  @Data
  public static class Otp {
    private int ttlMinutes;
    private int maxAttempts;
  }

  @Data
  public static class Jwt {
    private String secret;
    private String issuer;
    private long accessTtl;
    private long refreshTtl;
  }
}
