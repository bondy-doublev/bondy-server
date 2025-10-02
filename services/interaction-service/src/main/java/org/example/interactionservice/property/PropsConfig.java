package org.example.interactionservice.property;

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

    @Data
    public static class ApiKey {
        private String header;
        private String internal;
    }

    @Data
    public static class Gateway {
        private String url;
    }
}
