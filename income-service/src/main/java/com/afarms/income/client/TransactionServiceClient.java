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

    public TransactionServiceClient(RestClient.Builder restClientBuilder,
                                    @Value("${services.transaction.url}") String transactionServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(transactionServiceUrl).build();
    }

    public TransactionCreateResponseDTO createIncomeTransaction(TransactionCreateRequestDTO incomeTxRequest,
                                                                String authHeader) {
        try {
            TransactionCreateResponseDTO response = restClient.post()
                    .uri("/api/v1/transactions/income")
                    .header("Authorization", authHeader)
                    .body(buildBody(incomeTxRequest))
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

    private Object buildBody(TransactionCreateRequestDTO incomeTxRequest) {
        TransactionPayload payload = new TransactionPayload();
        payload.type = "INCOME";
        payload.referenceId = incomeTxRequest.getIncomeId();
        payload.date = incomeTxRequest.getOccurredAt();
        payload.amount = incomeTxRequest.getAmount();
        payload.createdBy = incomeTxRequest.getCreatedBy();
        payload.farmId = incomeTxRequest.getFarmId();
        payload.userId = incomeTxRequest.getUserId();
        return payload;
    }

    private static class TransactionPayload {
        public String type;
        public Long referenceId;
        public java.time.LocalDate date;
        public java.math.BigDecimal amount;
        public String createdBy;
        public java.util.UUID farmId;
        public java.util.UUID userId;
    }
}