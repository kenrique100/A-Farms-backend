package com.afarms.income.utils;

import com.afarms.income.model.dto.TokenValidationResponse;
import com.afarms.income.model.dto.TransactionCreateRequestDTO;
import com.afarms.income.model.entity.Income;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncomeBuilderUtilsTest {

    private final IncomeBuilderUtils incomeBuilderUtils = new IncomeBuilderUtils();

    @Test
    void buildTransactionRequest_shouldUseTokenUsernameAsCreatedBy() {
        UUID farmId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Income income = Income.builder()
                .id(10L)
                .description("Corn sales")
                .amount(new BigDecimal("250.00"))
                .occurredAt(LocalDate.of(2026, 5, 21))
                .farmId(farmId)
                .userId(userId)
                .build();

        TokenValidationResponse tokenInfo = new TokenValidationResponse(
                userId,
                "farmer@example.com",
                "farmer@example.com",
                true,
                "MASTER",
                farmId
        );

        TransactionCreateRequestDTO request = incomeBuilderUtils.buildTransactionRequest(income, tokenInfo);

        assertEquals(10L, request.getIncomeId());
        assertEquals("farmer@example.com", request.getCreatedBy());
        assertEquals(new BigDecimal("250.00"), request.getAmount());
        assertEquals(LocalDate.of(2026, 5, 21), request.getOccurredAt());
    }
}
