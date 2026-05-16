package com.afarms.transaction.utils;

import com.afarms.transaction.exception.BusinessException;
import com.afarms.transaction.exception.UnauthorizedException;
import com.afarms.transaction.model.dto.InternalIncomeTransactionRequestDTO;
import com.afarms.transaction.model.dto.TokenValidationResponse;
import com.afarms.transaction.model.dto.TransactionRequestDTO;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TransactionValidationUtils {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "MASTER", "SUB_USER", "USER");

    private final String internalApiKey;

    public TransactionValidationUtils(@Value("${app.integration.transaction-api-key:INSECURE_LOCAL_DEV_ONLY_CHANGE_ME_TRANSACTION_API_KEY}") String internalApiKey) {
        this.internalApiKey = internalApiKey;
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

    public void validateManualCreateRequest(TransactionRequestDTO request) {
        if (request == null) {
            throw new BusinessException("Transaction request cannot be null");
        }
        if (request.getOccurredAt() != null && request.getOccurredAt().isAfter(LocalDate.now())) {
            throw new BusinessException("Transaction occurredAt cannot be in the future");
        }
        if (request.getAmount() != null && request.getAmount().scale() > 2) {
            throw new BusinessException("Transaction amount cannot have more than 2 decimal places");
        }
    }

    public void validateInternalIncomeRequest(InternalIncomeTransactionRequestDTO request) {
        if (request == null) {
            throw new BusinessException("Internal income transaction request cannot be null");
        }
        if (request.getOccurredAt() != null && request.getOccurredAt().isAfter(LocalDate.now())) {
            throw new BusinessException("Income transaction occurredAt cannot be in the future");
        }
        if (request.getAmount() != null && request.getAmount().scale() > 2) {
            throw new BusinessException("Income transaction amount cannot have more than 2 decimal places");
        }
    }

    public void validateInternalApiKey(String providedApiKey) {
        if (providedApiKey == null || providedApiKey.isBlank() || !internalApiKey.equals(providedApiKey)) {
            throw new UnauthorizedException("Invalid internal API key");
        }
    }
}
