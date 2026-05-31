package com.afarms.investment.utils;

import com.afarms.investment.exception.AccessDeniedException;
import com.afarms.investment.exception.ResourceNotFoundException;
import com.afarms.investment.model.dto.TokenValidationResponse;
import com.afarms.investment.model.entity.Investment;
import com.afarms.investment.repository.InvestmentRepository;
import com.afarms.investment.security.JwtUtil;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvestmentServiceUtils {

    private final JwtUtil jwtUtil;
    private final InvestmentValidationUtils validationUtils;
    private final InvestmentRepository investmentRepository;

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

    public Investment findByIdAndFarmId(Long id, UUID farmId) {
        return investmentRepository.findByIdAndFarmId(id, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found with id: " + id));
    }

    public void checkOwnershipOrMaster(Investment investment, UUID userId, String role) {
        if (!investment.getUserId().equals(userId) && !"MASTER".equalsIgnoreCase(role)
                && !"ADMIN".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("You can only modify your own investments");
        }
    }

    public UUID resolveScopeFarmId(UUID requestedFarmId, TokenValidationResponse tokenInfo) {
        if (requestedFarmId == null) {
            return tokenInfo.getFarmId();
        }
        if (!requestedFarmId.equals(tokenInfo.getFarmId())
                && !"ADMIN".equalsIgnoreCase(tokenInfo.getRole())) {
            throw new AccessDeniedException("Cannot access investments from another farm");
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
            throw new AccessDeniedException("Cannot access investments from another user");
        }
        return requestedUserId;
    }
}
