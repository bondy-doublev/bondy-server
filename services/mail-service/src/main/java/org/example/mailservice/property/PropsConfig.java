package org.example.mailservice.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class PropsConfig {
    private String environment;
    private final Mail mail = new Mail();

    @Data
    public static class Mail {
        private String from;
        private String replyTo;
        private String baseUrl;
        private String templateCache;
        private Map<String, String> subjects;
    }
}
