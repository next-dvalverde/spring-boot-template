package es.nextdigital.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springdoc.core.models.GroupedOpenApi;

@Configuration
public class SwaggerConfig {

    // Configuración principal de Swagger/OpenAPI
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("cajero-api")
                .pathsToMatch("/**")
                .build();
    }
}