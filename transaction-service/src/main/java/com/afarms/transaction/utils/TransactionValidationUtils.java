package com.afarms.transaction.utils;

import com.afarms.transaction.exception.BusinessException;
import com.afarms.transaction.exception.UnauthorizedException;
import com.afarms.transaction.model.dto.TokenValidationResponse;
import com.afarms.transaction.model.dto.TransactionCreateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
public class TransactionValidationUtils {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "MASTER", "SUB_USER", "USER");
    private final String expectedApiKey;

    public TransactionValidationUtils(@Value("${app.integration.internal-api-key}") String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    public void validateInternalApiKey(String providedKey) {
        if (providedKey == null || providedKey.isBlank() || !expectedApiKey.equals(providedKey)) {
            throw new UnauthorizedException("Invalid internal API key");
        }
    }

    public void validateCreateRequest(TransactionCreateRequest request) {
        if (request == null) {
            throw new BusinessException("Transaction request cannot be null");
        }
        if (request.getFarmId() == null || request.getUserId() == null) {
            throw new BusinessException("FarmId and UserId are required");
        }
        if (request.getOccurredAt() != null && request.getOccurredAt().isAfter(LocalDate.now())) {
            throw new BusinessException("Transaction occurredAt cannot be in the future");
        }
        if (request.getAmount() != null && request.getAmount().scale() > 2) {
            throw new BusinessException("Transaction amount cannot have more than 2 decimal places");
        }
    }

    public void validateUserToken(TokenValidationResponse validation) {
        if (validation == null || !Boolean.TRUE.equals(validation.getIsValid())) {
            throw new BusinessException("Invalid user token");
        }
    }

    public void validateAuthorizationHeader(String authHeader) {
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
    }

    public void validateTokenPayload(TokenValidationResponse tokenInfo) {
        if (tokenInfo == null || tokenInfo.getUserId() == null || tokenInfo.getFarmId() == null) {
            throw new UnauthorizedException("Invalid token payload");
        }
        if (tokenInfo.getRole() == null || !ALLOWED_ROLES.contains(tokenInfo.getRole().toUpperCase())) {
            throw new UnauthorizedException("Invalid user role in token");
        }
    }
}