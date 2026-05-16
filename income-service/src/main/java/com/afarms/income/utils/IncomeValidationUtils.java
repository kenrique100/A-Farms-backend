package com.afarms.income.utils;

import com.afarms.income.exception.BusinessException;
import com.afarms.income.exception.UnauthorizedException;
import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.TokenValidationResponse;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class IncomeValidationUtils {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "MASTER", "SUB_USER", "USER");

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

    public void validateCreateIncomeRequest(IncomeRequestDTO request) {
        if (request == null) {
            throw new BusinessException("Income request cannot be null");
        }
        if (request.getOccurredAt() != null && request.getOccurredAt().isAfter(LocalDate.now())) {
            throw new BusinessException("Income occurredAt cannot be in the future");
        }
        if (request.getAmount() != null && request.getAmount().scale() > 2) {
            throw new BusinessException("Income amount cannot have more than 2 decimal places");
        }
    }
}
