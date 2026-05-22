package com.afarms.income.client;

import com.afarms.income.exception.ExternalServiceException;
import com.afarms.income.model.dto.TransactionCreateRequestDTO;
import com.afarms.income.model.dto.TransactionCreateResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Component
public class TransactionServiceClient {

    private final RestClient restClient;

    public TransactionServiceClient(RestClient.Builder restClientBuilder,
                                    @Value("${services.transaction.url}") String transactionServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(transactionServiceUrl).build();
    }

    public TransactionCreateResponseDTO createIncomeTransaction(TransactionCreateRequestDTO incomeTxRequest,
                                                                String authHeader) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("type", "INCOME");
            body.put("referenceId", incomeTxRequest.getIncomeId());
            body.put("date", incomeTxRequest.getOccurredAt());
            body.put("amount", incomeTxRequest.getAmount());
            body.put("createdBy", incomeTxRequest.getDescription());
            body.put("farmId", incomeTxRequest.getFarmId());
            body.put("userId", incomeTxRequest.getUserId());

            TransactionCreateResponseDTO response = restClient.post()
                    .uri("/api/v1/transactions/income")
                    .header("Authorization", authHeader)
                    .body(body)
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