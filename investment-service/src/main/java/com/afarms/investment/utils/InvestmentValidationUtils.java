package com.afarms.investment.utils;

import com.afarms.investment.exception.AccessDeniedException;
import com.afarms.investment.exception.BusinessException;
import com.afarms.investment.exception.UnauthorizedException;
import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.TokenValidationResponse;
import com.afarms.investment.model.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class InvestmentValidationUtils {

    public void validateAuthorizationHeader(String authHeader) {
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            log.warn("Authorization header missing or malformed");
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
    }

    public void validateTokenPayload(TokenValidationResponse tokenInfo) {
        if (tokenInfo == null || tokenInfo.getUserId() == null || tokenInfo.getFarmId() == null) {
            log.warn("Token payload is missing required fields");
            throw new UnauthorizedException("Invalid token payload");
        }
        if (tokenInfo.getRole() == null || !UserRole.isValid(tokenInfo.getRole())) {
            log.warn("Token contains invalid role: {}", tokenInfo.getRole());
            throw new UnauthorizedException("Invalid user role: " + tokenInfo.getRole());
        }
    }

    public void validateRoleCanWrite(String role) {
        if (!UserRole.isWriteAllowed(role)) {
            log.warn("Role '{}' attempted write operation without permission", role);
            throw new AccessDeniedException("User role '" + role + "' is not allowed to write investments");
        }
    }

    public void validateCreateRequest(InvestmentRequestDTO request) {
        if (request == null) {
            throw new BusinessException("Investment request cannot be null");
        }
        if (request.getInitialAmount() != null && request.getInitialAmount().scale() > 2) {
            throw new BusinessException("Initial amount cannot have more than 2 decimal places");
        }
        if (request.getCurrentBalance() != null && request.getCurrentBalance().scale() > 2) {
            throw new BusinessException("Current balance cannot have more than 2 decimal places");
        }
        if (request.getCurrentBalance() != null && request.getInitialAmount() != null
                && request.getCurrentBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Current balance cannot be negative");
        }
        log.debug("Investment request validated successfully");
    }

    public void validateUpdateBalance(BigDecimal newBalance) {
        if (newBalance == null) {
            throw new BusinessException("New balance is required");
        }
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("New balance cannot be negative");
        }
        if (newBalance.scale() > 2) {
            throw new BusinessException("Balance cannot have more than 2 decimal places");
        }
    }
}