package es.nextdigital.atm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI atmOpenApi() {
        // El esquema documenta el token opaco de POST /sessions para que
        // Swagger UI pueda autorizar; no es OAuth ni JWT (A-6).
        String scheme = "sessionToken";
        return new OpenAPI()
                .info(new Info()
                        .title("ATM API")
                        .description("API de cajeros automaticos - prueba tecnica NextDigital. "
                                + "El token se obtiene en POST /api/v1/sessions y caduca a los 120 s.")
                        .version("v1"))
                .components(new Components().addSecuritySchemes(scheme, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .description("Token opaco emitido por POST /api/v1/sessions")))
                .addSecurityItem(new SecurityRequirement().addList(scheme));
    }
}
