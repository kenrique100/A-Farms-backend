package com.afarms.investment.utils;

import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.TokenValidationResponse;
import com.afarms.investment.model.entity.Investment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InvestmentBuilderUtilsTest {

    private final InvestmentBuilderUtils builderUtils = new InvestmentBuilderUtils();

    @Test
    void buildFromRequest_shouldMapAllFields() {
        UUID farmId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        InvestmentRequestDTO request = new InvestmentRequestDTO(
                new BigDecimal("5000.00"), new BigDecimal("5500.00"));

        TokenValidationResponse tokenInfo = new TokenValidationResponse(
                userId, "investor", "investor@farm.com", true, "MASTER", farmId);

        Investment result = builderUtils.buildFromRequest(request, tokenInfo);

        assertNotNull(result);
        assertEquals(new BigDecimal("5000.00"), result.getInitialAmount());
        assertEquals(new BigDecimal("5500.00"), result.getCurrentBalance());
        assertEquals(farmId, result.getFarmId());
        assertEquals(userId, result.getUserId());

        // Use assertEquals with a tolerance for date comparison
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        // Check that dates are within 1 second of now
        LocalDate now = LocalDate.now();
        long daysBetweenCreated = ChronoUnit.DAYS.between(result.getCreatedAt(), now);
        long daysBetweenUpdated = ChronoUnit.DAYS.between(result.getUpdatedAt(), now);

        assertTrue(daysBetweenCreated == 0, "CreatedAt should be today");
        assertTrue(daysBetweenUpdated == 0, "UpdatedAt should be today");
    }

    @Test
    void updateBalance_shouldChangeBalanceAndUpdatedAt() {
        Investment investment = Investment.builder()
                .id(1L)
                .currentBalance(new BigDecimal("1000.00"))
                .updatedAt(LocalDate.of(2026, 1, 1))
                .build();

        builderUtils.updateBalance(investment, new BigDecimal("1500.00"));

        assertEquals(new BigDecimal("1500.00"), investment.getCurrentBalance());

        // Check that updatedAt is today (not a specific date)
        assertEquals(LocalDate.now(), investment.getUpdatedAt());
    }

    @Test
    void updateFromRequest_shouldUpdateAllMutableFields() {
        Investment investment = Investment.builder()
                .id(1L)
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("900.00"))
                .updatedAt(LocalDate.of(2026, 1, 1))
                .build();

        InvestmentRequestDTO request = new InvestmentRequestDTO(
                new BigDecimal("2000.00"), new BigDecimal("2100.00"));

        builderUtils.updateFromRequest(investment, request);

        assertEquals(new BigDecimal("2000.00"), investment.getInitialAmount());
        assertEquals(new BigDecimal("2100.00"), investment.getCurrentBalance());
        assertEquals(LocalDate.now(), investment.getUpdatedAt());
        assertEquals(1L, investment.getId());
    }
}