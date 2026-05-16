package com.afarms.income.utils;

import com.afarms.income.exception.AccessDeniedException;
import com.afarms.income.exception.BusinessException;
import com.afarms.income.exception.UnauthorizedException;
import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.TokenValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@Slf4j
public class IncomeValidationUtils {

    private static final Set<String> WRITE_ALLOWED_ROLES = Set.of("MASTER", "SUB_USER", "ADMIN");
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
            throw new UnauthorizedException("Invalid user role");
        }
    }

    public void validateCreateRequest(IncomeRequestDTO request) {
        if (request == null) {
            throw new BusinessException("Income request cannot be null");
        }
        if (request.getOccurredAt() != null && request.getOccurredAt().isAfter(LocalDate.now())) {
            throw new BusinessException("Occurred date cannot be in the future");
        }
        if (request.getAmount() != null && request.getAmount().scale() > 2) {
            throw new BusinessException("Amount cannot have more than 2 decimal places");
        }
    }

    public void validateRoleCanWrite(String role) {
        if (!WRITE_ALLOWED_ROLES.contains(role.toUpperCase())) {
            throw new AccessDeniedException("User role '" + role + "' is not allowed to write incomes");
        }
    }
}