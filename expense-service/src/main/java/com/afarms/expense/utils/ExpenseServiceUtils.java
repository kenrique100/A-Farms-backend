package com.afarms.expense.utils;

import com.afarms.expense.exception.AccessDeniedException;
import com.afarms.expense.exception.ResourceNotFoundException;
import com.afarms.expense.model.dto.TokenValidationResponse;
import com.afarms.expense.model.entity.Expense;
import com.afarms.expense.repository.ExpenseRepository;
import com.afarms.expense.security.JwtUtil;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpenseServiceUtils {

    private final JwtUtil jwtUtil;
    private final ExpenseValidationUtils validationUtils;
    private final ExpenseRepository expenseRepository;

    public TokenValidationResponse validateAndGetTokenInfo(String authHeader) {
        validationUtils.validateAuthorizationHeader(authHeader);
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new AccessDeniedException("Invalid or expired token");
        }

        Claims claims = jwtUtil.extractClaims(token);
        String userIdStr = claims.get("userId", String.class);
        String farmIdStr = claims.get("farmId", String.class);
        String role = claims.get("role", String.class);
        String username = claims.getSubject();

        TokenValidationResponse response = new TokenValidationResponse(
                UUID.fromString(userIdStr), username, role, UUID.fromString(farmIdStr));
        validationUtils.validateTokenPayload(response);
        return response;
    }

    public TokenValidationResponse validateAndGetTokenInfoWithWriteAccess(String authHeader) {
        TokenValidationResponse tokenInfo = validateAndGetTokenInfo(authHeader);
        validationUtils.validateRoleCanWrite(tokenInfo.getRole());
        return tokenInfo;
    }

    public Expense findByIdAndFarmId(Long id, UUID farmId) {
        return expenseRepository.findByIdAndFarmId(id, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    public void checkOwnershipOrMaster(Expense expense, UUID userId, String role) {
        if (!expense.getUserId().equals(userId) && !"MASTER".equalsIgnoreCase(role)
                && !"ADMIN".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("You can only modify your own expenses");
        }
    }

    public UUID resolveScopeFarmId(UUID requestedFarmId, TokenValidationResponse tokenInfo) {
        if (requestedFarmId == null) {
            return tokenInfo.getFarmId();
        }
        if (!requestedFarmId.equals(tokenInfo.getFarmId())
                && !"ADMIN".equalsIgnoreCase(tokenInfo.getRole())) {
            throw new AccessDeniedException("Cannot access expenses from another farm");
        }
        return requestedFarmId;
    }

    public UUID resolveScopeUserId(UUID requestedUserId, TokenValidationResponse tokenInfo) {
        if (requestedUserId == null) {
            return null;
        }
        if (!requestedUserId.equals(tokenInfo.getUserId())
                && !"MASTER".equalsIgnoreCase(tokenInfo.getRole())
                && !"ADMIN".equalsIgnoreCase(tokenInfo.getRole())) {
            throw new AccessDeniedException("Cannot access expenses from another user");
        }
        return requestedUserId;
    }
}
