package com.afarms.transaction.client;

import com.afarms.transaction.model.dto.ExpensePageResponse;
import com.afarms.transaction.model.dto.ExpenseTransactionDTO;
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
public class ExpenseServiceClient {

    private final RestClient restClient;

    public ExpenseServiceClient(RestClient.Builder restClientBuilder,
                                @Value("${services.expense.url}") String expenseServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(expenseServiceUrl).build();
    }

    @CircuitBreaker(name = "expenseService", fallbackMethod = "expenseFallback")
    @Retry(name = "expenseServiceRetry")
    public List<ExpenseTransactionDTO> getExpensesByDateRange(String authHeader, LocalDate start, LocalDate end) {
        ExpensePageResponse response = restClient.get()
                .uri("/api/v1/expenses/range?start={start}&end={end}&page=0&size=1000", start, end)
                .header("Authorization", authHeader)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("Expense service 4xx: " + res.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new RuntimeException("Expense service 5xx: " + res.getStatusCode());
                })
                .body(ExpensePageResponse.class);

        if (response != null && response.getContent() != null) {
            log.info("Fetched {} expense records for range {}-{}", response.getContent().size(), start, end);
            return response.getContent();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unused")
    public List<ExpenseTransactionDTO> expenseFallback(String authHeader, LocalDate start,
                                                       LocalDate end, Throwable t) {
        log.error("Expense service circuit open or retry exhausted: {}", t.getMessage());
        return Collections.emptyList();
    }
}