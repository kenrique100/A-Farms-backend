package com.afarms.income.config;

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

    @Value("${server.port}")
    private int serverPort;

    @Bean
    public OpenAPI incomeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Income Service API")
                        .description("Income management, tracking and reporting service for FarmStack")
                        .version("1.0")
                        .contact(new Contact()
                                .name("FarmStack Support")
                                .email("support@farmstack.com")
                                .url("https://farmstack.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://farmstack.com/terms")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Direct access to Income Service"),
                        new Server()
                                .url("http://localhost:8080/api/v1/incomes")
                                .description("Via API Gateway"),
                        new Server()
                                .url("${API_GATEWAY_URL:/api/v1/incomes}")
                                .description("Docker environment via Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .schemaRequirement("BearerAuth",
                        new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from User Service authentication"));
    }
}