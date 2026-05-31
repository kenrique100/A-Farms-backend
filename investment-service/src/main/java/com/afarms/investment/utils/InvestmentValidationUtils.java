package com.afarms.investment.utils;

import com.afarms.investment.exception.AccessDeniedException;
import com.afarms.investment.exception.BusinessException;
import com.afarms.investment.exception.UnauthorizedException;
import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.TokenValidationResponse;
import com.afarms.investment.model.enums.UserRole;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class InvestmentValidationUtils {

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

    public void validateCreateRequest(InvestmentRequestDTO request) {
        if (request == null) {
            throw new BusinessException("Investment request cannot be null");
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
            throw new AccessDeniedException("User role '" + role + "' is not allowed to write investments");
        }
    }
}
