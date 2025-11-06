package org.example.notificationservice.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
  servers = {
    @Server(url = "/api/v1", description = "Via Gateway"),
    @Server(url = "/", description = "Direct service")
  }
)
@Configuration
public class SwaggerServersConfig {
}
