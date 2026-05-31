package com.afarms.transaction.utils;

import com.afarms.transaction.exception.BusinessException;
import com.afarms.transaction.model.dto.TokenValidationResponse;
import com.afarms.transaction.model.entity.Transaction;
import com.afarms.transaction.repository.TransactionRepository;
import com.afarms.transaction.security.JwtUtil;
import io.jsonwebtoken.Claims;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionServiceUtils {

    private final JwtUtil jwtUtil;
    private final TransactionValidationUtils validationUtils;
    private final TransactionRepository transactionRepository;

    public TokenValidationResponse validateAndGetTokenInfo(String authHeader) {
        validationUtils.validateAuthorizationHeader(authHeader);
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            throw new BusinessException("Invalid or expired token");
        }

        Claims claims = jwtUtil.extractClaims(token);
        String userIdStr = claims.get("userId", String.class);
        String farmIdStr = claims.get("farmId", String.class);
        String role = claims.get("role", String.class);
        String username = claims.getSubject();

        TokenValidationResponse tokenInfo = new TokenValidationResponse(
                UUID.fromString(userIdStr), username, role, UUID.fromString(farmIdStr));
        validationUtils.validateTokenPayload(tokenInfo);
        return tokenInfo;
    }

    public TokenValidationResponse validateAndGetTokenInfoWithWriteAccess(String authHeader) {
        TokenValidationResponse tokenInfo = validateAndGetTokenInfo(authHeader);
        validationUtils.validateRoleCanWrite(tokenInfo.getRole());
        return tokenInfo;
    }

    public UUID resolveScopeFarmId(UUID requestedFarmId, TokenValidationResponse tokenInfo) {
        if (requestedFarmId == null) {
            return tokenInfo.getFarmId();
        }
        if (!requestedFarmId.equals(tokenInfo.getFarmId()) && !"ADMIN".equalsIgnoreCase(tokenInfo.getRole())) {
            throw new BusinessException("Cannot access transactions from another farm");
        }
        return requestedFarmId;
    }

    public Optional<Transaction> findIdempotent(String type, Long referenceId, UUID farmId) {
        return transactionRepository.findByTypeAndReferenceIdAndFarmId(type, referenceId, farmId);
    }
}
