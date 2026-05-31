package com.afarms.expense.client;

import com.afarms.expense.exception.ExternalServiceException;
import com.afarms.expense.model.dto.TransactionCreateRequestDTO;
import com.afarms.expense.model.dto.TransactionCreateResponseDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TransactionServiceClient {

    private final RestClient restClient;

    public TransactionServiceClient(RestClient.Builder restClientBuilder,
                                    @Value("${services.transaction.url}") String transactionServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(transactionServiceUrl).build();
    }

    @Retry(name = "transactionServiceRetry")
    @CircuitBreaker(name = "transactionService")
    public TransactionCreateResponseDTO createExpenseTransaction(
            TransactionCreateRequestDTO request,
            String authHeader) {
        try {
            TransactionCreateResponseDTO response = restClient.post()
                    .uri("/api/v1/transactions/expense")
                    .header("Authorization", authHeader)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new ExternalServiceException("Transaction service rejected request");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new ExternalServiceException("Transaction service unavailable");
                    })
                    .body(TransactionCreateResponseDTO.class);

            if (response == null || response.getId() == null) {
                throw new ExternalServiceException("Invalid transaction response");
            }
            return response;
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Failed to communicate with transaction service", ex);
        }
    }
}
