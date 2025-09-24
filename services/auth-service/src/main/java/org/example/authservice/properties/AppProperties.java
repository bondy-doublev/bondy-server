package org.example.authservice.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppProperties {
    String environment;
    final User user = new User();
    final Otp otp = new Otp();

    @Data
    public static class User {
        private String defaultPasswordSuffix;
    }

    @Data
    public static class Otp {
        private int ttlMinutes;
        private int maxAttempts;
    }
}
