package co.edu.monteria.medicitas.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("MediCitas Montería - API REST")
                        .version("1.0.0")
                        .description("Plataforma móvil de agendamiento de citas médicas para Montería, Córdoba. " +
                                "Cumplimiento legal Res. 1995/1999 y Res. 3280/2018 para historias clínicas y verificación BDUA EPS.")
                        .contact(new Contact()
                                .name("Universidad de Córdoba - Desarrollo de Apps")
                                .email("desarrolloapps@unicordoba.edu.co")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
