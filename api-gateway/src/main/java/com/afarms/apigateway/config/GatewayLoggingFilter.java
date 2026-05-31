package com.afarms.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Component
@Slf4j
public class GatewayLoggingFilter implements GlobalFilter, Ordered {
    private static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
            request = request.mutate().header(CORRELATION_ID, correlationId).build();
        }
        exchange.getResponse().getHeaders().set(CORRELATION_ID, correlationId);
        ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();

        String finalCorrelationId = correlationId;
        return chain.filter(mutatedExchange)
                .doFinally(signal -> {
                    long duration = System.currentTimeMillis() - start;
                    int status = exchange.getResponse().getStatusCode() == null
                            ? 0
                            : exchange.getResponse().getStatusCode().value();
                    log.info("Gateway {} {} -> status={} durationMs={} correlationId={}",
                            request.getMethod(), request.getURI().getPath(), status, duration, finalCorrelationId);
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
