package com.afarms.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("income-service", r -> r.path("/api/v1/incomes/**")
                        .uri("http://income-service:8082"))
                .route("expense-service", r -> r.path("/api/v1/expenses/**")
                        .uri("http://expense-service:8083"))
                .route("investment-service", r -> r.path("/api/v1/investments/**")
                        .uri("http://investment-service:8084"))
                .route("transaction-service", r -> r.path("/api/v1/transactions/**")
                        .uri("http://transaction-service:8085"))
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .uri("http://user-service:8086"))
                .build();
    }
}