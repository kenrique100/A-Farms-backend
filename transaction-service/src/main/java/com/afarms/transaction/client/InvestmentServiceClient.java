package com.afarms.transaction.client;

import com.afarms.transaction.model.dto.InvestmentPageResponse;
import com.afarms.transaction.model.dto.InvestmentTransactionDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class InvestmentServiceClient {

    private final RestClient restClient;

    public InvestmentServiceClient(RestClient.Builder restClientBuilder,
                                   @Value("${services.investment.url}") String investmentServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(investmentServiceUrl).build();
    }

    @CircuitBreaker(name = "investmentService", fallbackMethod = "investmentFallback")
    @Retry(name = "investmentServiceRetry")
    public List<InvestmentTransactionDTO> getAllInvestments(String authHeader) {
        InvestmentPageResponse response = restClient.get()
                .uri("/api/v1/investments?page=0&size=1000")
                .header("Authorization", authHeader)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("Investment service 4xx: " + res.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new RuntimeException("Investment service 5xx: " + res.getStatusCode());
                })
                .body(InvestmentPageResponse.class);

        if (response != null && response.getContent() != null) {
            log.info("Fetched {} investment records", response.getContent().size());
            return response.getContent();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unused")
    public List<InvestmentTransactionDTO> investmentFallback(String authHeader, Throwable t) {
        log.error("Investment service circuit open or retry exhausted: {}", t.getMessage());
        return Collections.emptyList();
    }
}