package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String ENCODING_SCHEME = "bearer";
    private static final String SECURITY_SCHEME = "BearerAuth";
    private static final String BEARER_FORMAT = "JWT";

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(ENCODING_SCHEME)
                                .bearerFormat(BEARER_FORMAT)))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    }
}
