package com.afarms.investment.utils;

import com.afarms.investment.exception.AccessDeniedException;
import com.afarms.investment.exception.BusinessException;
import com.afarms.investment.exception.UnauthorizedException;
import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.TokenValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InvestmentValidationUtilsTest {

    private InvestmentValidationUtils validationUtils;

    @BeforeEach
    void setUp() {
        validationUtils = new InvestmentValidationUtils();
    }

    @Test
    void validateAuthorizationHeader_withValidHeader_shouldPass() {
        assertDoesNotThrow(() ->
                validationUtils.validateAuthorizationHeader("Bearer valid-token"));
    }

    @Test
    void validateAuthorizationHeader_withNullHeader_shouldThrow() {
        assertThrows(UnauthorizedException.class,
                () -> validationUtils.validateAuthorizationHeader(null));
    }

    @Test
    void validateAuthorizationHeader_withBlankHeader_shouldThrow() {
        assertThrows(UnauthorizedException.class,
                () -> validationUtils.validateAuthorizationHeader(""));
    }

    @Test
    void validateAuthorizationHeader_withoutBearer_shouldThrow() {
        assertThrows(UnauthorizedException.class,
                () -> validationUtils.validateAuthorizationHeader("invalid-token"));
    }

    @Test
    void validateTokenPayload_withValidToken_shouldPass() {
        TokenValidationResponse tokenInfo = new TokenValidationResponse(
                UUID.randomUUID(), "user", "user@email.com", true, "MASTER", UUID.randomUUID());

        assertDoesNotThrow(() -> validationUtils.validateTokenPayload(tokenInfo));
    }

    @Test
    void validateTokenPayload_withNullToken_shouldThrow() {
        assertThrows(UnauthorizedException.class,
                () -> validationUtils.validateTokenPayload(null));
    }

    @Test
    void validateTokenPayload_withInvalidRole_shouldThrow() {
        TokenValidationResponse tokenInfo = new TokenValidationResponse(
                UUID.randomUUID(), "user", "user@email.com", true, "INVALID_ROLE", UUID.randomUUID());

        assertThrows(UnauthorizedException.class,
                () -> validationUtils.validateTokenPayload(tokenInfo));
    }

    @Test
    void validateRoleCanWrite_withMasterRole_shouldPass() {
        assertDoesNotThrow(() -> validationUtils.validateRoleCanWrite("MASTER"));
    }

    @Test
    void validateRoleCanWrite_withViewerRole_shouldThrow() {
        assertThrows(AccessDeniedException.class,
                () -> validationUtils.validateRoleCanWrite("VIEWER"));
    }

    @Test
    void validateCreateRequest_withValidRequest_shouldPass() {
        InvestmentRequestDTO request = InvestmentRequestDTO.builder()
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("1000.00"))
                .build();

        assertDoesNotThrow(() -> validationUtils.validateCreateRequest(request));
    }

    @Test
    void validateCreateRequest_withNullRequest_shouldThrow() {
        assertThrows(BusinessException.class,
                () -> validationUtils.validateCreateRequest(null));
    }

    @Test
    void validateCreateRequest_withNegativeBalance_shouldThrow() {
        InvestmentRequestDTO request = InvestmentRequestDTO.builder()
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("-100.00"))
                .build();

        assertThrows(BusinessException.class,
                () -> validationUtils.validateCreateRequest(request));
    }

    @Test
    void validateUpdateBalance_withValidBalance_shouldPass() {
        assertDoesNotThrow(() ->
                validationUtils.validateUpdateBalance(new BigDecimal("1500.00")));
    }

    @Test
    void validateUpdateBalance_withNullBalance_shouldThrow() {
        assertThrows(BusinessException.class,
                () -> validationUtils.validateUpdateBalance(null));
    }

    @Test
    void validateUpdateBalance_withNegativeBalance_shouldThrow() {
        assertThrows(BusinessException.class,
                () -> validationUtils.validateUpdateBalance(new BigDecimal("-100.00")));
    }
}