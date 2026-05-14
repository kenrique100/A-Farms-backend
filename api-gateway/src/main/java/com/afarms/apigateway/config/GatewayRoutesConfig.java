package com.afarms.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${services.income.url:http://income-service:8080}")
    private String incomeServiceUrl;

    @Value("${services.expense.url:http://expense-service:8080}")
    private String expenseServiceUrl;

    @Value("${services.investment.url:http://investment-service:8080}")
    private String investmentServiceUrl;

    @Value("${services.transaction.url:http://transaction-service:8080}")
    private String transactionServiceUrl;

    @Value("${services.user.url:http://user-service:8080}")
    private String userServiceUrl;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service-docs", r -> r
                        .path("/user-service/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/user-service/(?<segment>.*)", "/${segment}"))
                        .uri(userServiceUrl))
                .route("income-service", r -> r.path("/api/v1/incomes/**")
                        .uri(incomeServiceUrl))
                .route("expense-service", r -> r.path("/api/v1/expenses/**")
                        .uri(expenseServiceUrl))
                .route("investment-service", r -> r.path("/api/v1/investments/**")
                        .uri(investmentServiceUrl))
                .route("transaction-service", r -> r.path("/api/v1/transactions/**")
                        .uri(transactionServiceUrl))
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .uri(userServiceUrl))
                .build();
    }
}
