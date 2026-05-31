package com.afarms.transaction.client;

import com.afarms.transaction.model.dto.IncomePageResponse;
import com.afarms.transaction.model.dto.IncomeTransactionDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class IncomeServiceClient {

    private final RestClient restClient;

    public IncomeServiceClient(RestClient.Builder restClientBuilder,
                               @Value("${services.income.url}") String incomeServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(incomeServiceUrl).build();
    }

    @CircuitBreaker(name = "incomeService", fallbackMethod = "incomeFallback")
    @Retry(name = "incomeServiceRetry")
    public List<IncomeTransactionDTO> getIncomesByDateRange(String authHeader, LocalDate start, LocalDate end) {
        IncomePageResponse response = restClient.get()
                .uri("/api/v1/incomes/range?start={start}&end={end}&page=0&size=1000", start, end)
                .header("Authorization", authHeader)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("Income service 4xx: " + res.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new RuntimeException("Income service 5xx: " + res.getStatusCode());
                })
                .body(IncomePageResponse.class);

        if (response != null && response.getContent() != null) {
            log.info("Fetched {} income records for range {}-{}", response.getContent().size(), start, end);
            return response.getContent();
        }
        return Collections.emptyList();
    }

    // Resilience-4j requires fallback signature to exactly match the primary method + Throwable
    // Parameters are intentionally kept even if unused in the body — required by the framework
    @SuppressWarnings("unused")
    public List<IncomeTransactionDTO> incomeFallback(String authHeader, LocalDate start,
                                                     LocalDate end, Throwable t) {
        log.error("Income service circuit open or retry exhausted: {}", t.getMessage());
        return Collections.emptyList();
    }
}