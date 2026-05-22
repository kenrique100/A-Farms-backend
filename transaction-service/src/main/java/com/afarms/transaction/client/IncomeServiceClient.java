package com.afarms.transaction.client;

import com.afarms.transaction.model.dto.IncomePageResponse;
import com.afarms.transaction.model.dto.IncomeTransactionDTO;
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

    public List<IncomeTransactionDTO> getIncomesByDateRange(String authHeader, LocalDate start, LocalDate end) {
        try {
            IncomePageResponse response = restClient.get()
                    .uri("/api/v1/incomes/range?start={start}&end={end}&page=0&size=1000", start, end)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) ->
                            log.warn("Income service returned 4xx for date range {}-{}", start, end))
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) ->
                            log.error("Income service returned 5xx for date range {}-{}", start, end))
                    .body(IncomePageResponse.class);

            if (response != null && response.getContent() != null) {
                return response.getContent();
            }
        } catch (Exception e) {
            log.error("Failed to fetch incomes from income service: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }
}