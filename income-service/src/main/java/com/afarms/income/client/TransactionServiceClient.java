package com.afarms.income.client;

import com.afarms.income.exception.ExternalServiceException;
import com.afarms.income.model.dto.TransactionCreateRequestDTO;
import com.afarms.income.model.dto.TransactionCreateResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TransactionServiceClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public TransactionServiceClient(RestClient.Builder restClientBuilder,
                                    @Value("${services.transaction.url:http://localhost:8084}") String transactionServiceUrl,
                                    @Value("${app.integration.transaction-api-key:INSECURE_LOCAL_DEV_ONLY_CHANGE_ME_TRANSACTION_API_KEY}") String internalApiKey) {
        this.restClient = restClientBuilder.baseUrl(transactionServiceUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public TransactionCreateResponseDTO createIncomeTransaction(TransactionCreateRequestDTO request) {
        try {
            TransactionCreateResponseDTO response = restClient.post()
                    .uri("/api/v1/transactions/internal/income")
                    .header("X-Internal-Api-Key", internalApiKey)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new ExternalServiceException("Transaction-service rejected income transaction request");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new ExternalServiceException("Transaction-service is currently unavailable");
                    })
                    .body(TransactionCreateResponseDTO.class);

            if (response == null || response.getId() == null) {
                throw new ExternalServiceException("Transaction-service returned invalid transaction response");
            }
            return response;
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Failed to communicate with transaction-service", ex);
        }
    }
}
