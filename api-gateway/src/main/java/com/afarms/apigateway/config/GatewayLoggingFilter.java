package com.afarms.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GatewayLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        return chain.filter(exchange)
                .doFinally(signal -> {
                    long duration = System.currentTimeMillis() - start;
                    int status = exchange.getResponse().getStatusCode() == null
                            ? 0
                            : exchange.getResponse().getStatusCode().value();
                    log.info("Gateway {} {} -> status={} durationMs={}",
                            request.getMethod(), request.getURI().getPath(), status, duration);
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
