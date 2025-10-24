package org.example.communicationservice.config.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.websocket")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebsocketProps {
  // default nếu thiếu cấu hình
  List<String> allowedOrigins = new ArrayList<>(List.of(
    "http://localhost:3000",
    "http://127.0.0.1:3000"
  ));
}