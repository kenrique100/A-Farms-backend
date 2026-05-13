package com.afarms.apigateway.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi incomeApi() {
        return GroupedOpenApi.builder()
                .group("income-service")
                .pathsToMatch("/api/v1/incomes/**")
                .build();
    }

    @Bean
    public GroupedOpenApi expenseApi() {
        return GroupedOpenApi.builder()
                .group("expense-service")
                .pathsToMatch("/api/v1/expenses/**")
                .build();
    }

    @Bean
    public GroupedOpenApi investmentApi() {
        return GroupedOpenApi.builder()
                .group("investment-service")
                .pathsToMatch("/api/v1/investments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi transactionApi() {
        return GroupedOpenApi.builder()
                .group("transaction-service")
                .pathsToMatch("/api/v1/transactions/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user-service")
                .pathsToMatch("/api/v1/users/**")
                .build();
    }
}