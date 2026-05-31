package com.afarms.income.utils;

import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.TokenValidationResponse;
import com.afarms.income.model.entity.Income;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IncomeBuilderUtilsTest {

    private final IncomeBuilderUtils incomeBuilderUtils = new IncomeBuilderUtils();

    @Test
    void buildIncomeFromRequest_shouldMapAllFieldsFromRequestAndToken() {
        UUID farmId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        IncomeRequestDTO request = new IncomeRequestDTO(
                "Corn sales",
                new BigDecimal("250.00"),
                LocalDate.of(2026, 5, 21)
        );

        TokenValidationResponse tokenInfo = new TokenValidationResponse(
                userId,
                "farmer_john",
                "farmer@example.com",
                true,
                "MASTER",
                farmId
        );

        Income income = incomeBuilderUtils.buildIncomeFromRequest(request, tokenInfo);

        assertNotNull(income);
        assertEquals("Corn sales", income.getDescription());
        assertEquals(new BigDecimal("250.00"), income.getAmount());
        assertEquals(LocalDate.of(2026, 5, 21), income.getOccurredAt());
        assertEquals(farmId, income.getFarmId());
        assertEquals(userId, income.getUserId());
    }

    @Test
    void updateIncomeFromRequest_shouldOverwriteDescriptionAmountAndDate() {
        Income existing = Income.builder()
                .id(1L)
                .description("Old description")
                .amount(new BigDecimal("100.00"))
                .occurredAt(LocalDate.of(2026, 1, 1))
                .build();

        IncomeRequestDTO update = new IncomeRequestDTO(
                "Updated description",
                new BigDecimal("999.99"),
                LocalDate.of(2026, 5, 21)
        );

        incomeBuilderUtils.updateIncomeFromRequest(existing, update);

        assertEquals("Updated description", existing.getDescription());
        assertEquals(new BigDecimal("999.99"), existing.getAmount());
        assertEquals(LocalDate.of(2026, 5, 21), existing.getOccurredAt());
        assertEquals(1L, existing.getId()); // id must not change
    }
}