package org.example.mailservice.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String JWT_SCHEME_NAME = "JWT Bearer";
    private static final String APIKEY_SCHEME_NAME = "API Key";

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mail Service API")
                        .version("v1")
                        .description("REST API for Bondy"))
                .components(new Components()
                        .addSecuritySchemes(JWT_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Use JWT token in the Authorization header: Bearer <token>"))
                        .addSecuritySchemes(APIKEY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-KEY")
                                .description("Internal access key via X-API-KEY header")))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME_NAME))
                .addSecurityItem(new SecurityRequirement().addList(APIKEY_SCHEME_NAME));
    }
}

