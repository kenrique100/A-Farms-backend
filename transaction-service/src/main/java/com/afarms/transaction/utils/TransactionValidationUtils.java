package com.afarms.transaction.utils;

import com.afarms.transaction.exception.BusinessException;
import com.afarms.transaction.exception.UnauthorizedException;
import com.afarms.transaction.model.dto.TokenValidationResponse;
import com.afarms.transaction.model.dto.TransactionCreateRequestDTO;
import com.afarms.transaction.model.enums.UserRole;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class TransactionValidationUtils {

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

    public void validateCreateRequest(TransactionCreateRequestDTO request) {
        if (request == null) {
            throw new BusinessException("Transaction request cannot be null");
        }
        if (request.getDate() != null && request.getDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Transaction date cannot be in the future");
        }
    }

    public void validateRoleCanWrite(String role) {
        if (!UserRole.isWriteAllowed(role)) {
            throw new BusinessException("User role '" + role + "' is not allowed to write transactions");
        }
    }
}
