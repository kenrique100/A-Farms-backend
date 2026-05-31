package com.afarms.investment.utils;

import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.model.entity.Investment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResponseBuilderUtilsTest {

    private final ResponseBuilderUtils responseBuilderUtils = new ResponseBuilderUtils();

    @Test
    void buildResponse_shouldComputePositiveRoi() {
        Investment investment = Investment.builder()
                .id(1L)
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("1200.00"))
                .farmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .createdAt(LocalDate.now())
                .build();

        InvestmentResponseDTO dto = responseBuilderUtils.buildResponse(investment, "investor_user");

        assertEquals(new BigDecimal("20.00"), dto.getRoi()); // (200/1000)*100 = 20%
        assertEquals("investor_user", dto.getUserName());
        assertEquals(1L, dto.getId());
        assertEquals(new BigDecimal("1000.00"), dto.getInitialAmount());
        assertEquals(new BigDecimal("1200.00"), dto.getCurrentBalance());
    }

    @Test
    void buildResponse_shouldComputeNegativeRoi() {
        Investment investment = Investment.builder()
                .id(2L)
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("800.00"))
                .farmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .createdAt(LocalDate.now())
                .build();

        InvestmentResponseDTO dto = responseBuilderUtils.buildResponse(investment, "farmer");

        assertEquals(new BigDecimal("-20.00"), dto.getRoi()); // loss of 20%
        assertEquals("farmer", dto.getUserName());
    }

    @Test
    void buildResponse_shouldReturnZeroRoiWhenInitialAmountIsZero() {
        Investment investment = Investment.builder()
                .id(3L)
                .initialAmount(BigDecimal.ZERO)
                .currentBalance(new BigDecimal("500.00"))
                .farmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .createdAt(LocalDate.now())
                .build();

        InvestmentResponseDTO dto = responseBuilderUtils.buildResponse(investment, "farmer");

        assertEquals(BigDecimal.ZERO, dto.getRoi());
    }

    @Test
    void buildResponse_shouldReturnZeroRoiWhenInitialAmountIsNull() {
        Investment investment = Investment.builder()
                .id(4L)
                .initialAmount(null)
                .currentBalance(new BigDecimal("500.00"))
                .farmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .createdAt(LocalDate.now())
                .build();

        InvestmentResponseDTO dto = responseBuilderUtils.buildResponse(investment, "farmer");

        assertEquals(BigDecimal.ZERO, dto.getRoi());
    }

    @Test
    void buildResponse_shouldReturnZeroRoiWhenCurrentBalanceIsNull() {
        Investment investment = Investment.builder()
                .id(5L)
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(null)
                .farmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .createdAt(LocalDate.now())
                .build();

        InvestmentResponseDTO dto = responseBuilderUtils.buildResponse(investment, "farmer");

        assertEquals(BigDecimal.ZERO, dto.getRoi());
    }

    @Test
    void buildResponse_shouldComputeRoiWithScaling() {
        Investment investment = Investment.builder()
                .id(6L)
                .initialAmount(new BigDecimal("2500.00"))
                .currentBalance(new BigDecimal("3125.00"))
                .farmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .createdAt(LocalDate.now())
                .build();

        InvestmentResponseDTO dto = responseBuilderUtils.buildResponse(investment, "investor");

        // (3125 - 2500) / 2500 * 100 = 25.00%
        assertEquals(new BigDecimal("25.00"), dto.getRoi());
    }

    @Test
    void buildResponse_shouldHandleUsernameFallback() {
        Investment investment = Investment.builder()
                .id(7L)
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("1000.00"))
                .farmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .createdAt(LocalDate.now())
                .build();

        // Test with null username
        InvestmentResponseDTO dto1 = responseBuilderUtils.buildResponse(investment, null);
        assertNull(dto1.getUserName());

        // Test with blank username
        InvestmentResponseDTO dto2 = responseBuilderUtils.buildResponse(investment, "");
        assertEquals("", dto2.getUserName());

        // Test with valid username
        InvestmentResponseDTO dto3 = responseBuilderUtils.buildResponse(investment, "john_doe");
        assertEquals("john_doe", dto3.getUserName());
    }
}