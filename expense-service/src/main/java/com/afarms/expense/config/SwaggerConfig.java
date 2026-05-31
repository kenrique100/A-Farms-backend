package com.afarms.expense.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8082}")
    private int serverPort;

    @Bean
    public OpenAPI expenseServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Expense Service API")
                        .description("Expense management for farms – Master & Sub‑user CRUD")
                        .version("1.0")
                        .contact(new Contact().name("FarmStack").email("support@farmstack.com"))
                        .license(new License().name("Proprietary")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Direct"),
                        new Server().url("http://localhost:8082/api/v1/expenses").description("Via Gateway")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .schemaRequirement("BearerAuth", new SecurityScheme()
                        .name("BearerAuth").type(SecurityScheme.Type.HTTP)
                        .scheme("bearer").bearerFormat("JWT"));
    }
}