package com.afarms.income.utils;

import com.afarms.income.exception.AccessDeniedException;
import com.afarms.income.exception.BusinessException;
import com.afarms.income.exception.UnauthorizedException;
import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.TokenValidationResponse;
import com.afarms.income.model.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
public class IncomeValidationUtils {

    public void validateAuthorizationHeader(String authHeader) {
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
    }

    public void validateTokenPayload(TokenValidationResponse tokenInfo) {
        if (tokenInfo == null || tokenInfo.getUserId() == null || tokenInfo.getFarmId() == null) {
            throw new UnauthorizedException("Invalid token payload");
        }
        if (tokenInfo.getRole() == null || !UserRole.isValid(tokenInfo.getRole())) {
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
        if (!UserRole.isWriteAllowed(role)) {
            throw new AccessDeniedException("User role '" + role + "' is not allowed to write incomes");
        }
    }
}