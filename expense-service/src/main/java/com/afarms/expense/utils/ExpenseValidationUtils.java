package com.afarms.expense.utils;

import com.afarms.expense.exception.AccessDeniedException;
import com.afarms.expense.exception.BusinessException;
import com.afarms.expense.exception.UnauthorizedException;
import com.afarms.expense.model.dto.ExpenseRequestDTO;
import com.afarms.expense.model.dto.TokenValidationResponse;
import com.afarms.expense.model.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
public class ExpenseValidationUtils {

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

    public void validateCreateRequest(ExpenseRequestDTO request) {
        if (request == null) {
            throw new BusinessException("Expense request cannot be null");
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
            throw new AccessDeniedException("User role '" + role + "' is not allowed to write expenses");
        }
    }
}