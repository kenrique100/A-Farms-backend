package com.afarms.income.utils;

import com.afarms.income.exception.AccessDeniedException;
import com.afarms.income.exception.ResourceNotFoundException;
import com.afarms.income.model.dto.IncomeResponseDTO;
import com.afarms.income.model.dto.TokenValidationResponse;
import com.afarms.income.model.entity.Income;
import com.afarms.income.repository.IncomeRepository;
import com.afarms.income.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceUtils {

    private final JwtUtil jwtUtil;
    private final IncomeValidationUtils validationUtils;
    private final ResponseBuilderUtils responseBuilder;
    private final IncomeRepository incomeRepository;

    public TokenValidationResponse validateAndGetTokenInfo(String authHeader) {
        validationUtils.validateAuthorizationHeader(authHeader);

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            log.warn("JWT token failed local validation");
            throw new AccessDeniedException("Invalid or expired token");
        }

        Claims claims = jwtUtil.extractClaims(token);

        String userIdStr = claims.get("userId", String.class);
        String farmIdStr = claims.get("farmId", String.class);
        String role = claims.get("role", String.class);
        String username = claims.getSubject();

        if (userIdStr == null || farmIdStr == null) {
            log.warn("Token missing userId or farmId claims");
            throw new AccessDeniedException("Invalid token payload");
        }

        TokenValidationResponse tokenInfo = new TokenValidationResponse(
                UUID.fromString(userIdStr),
                username,
                username,
                true,
                role,
                UUID.fromString(farmIdStr)
        );

        validationUtils.validateTokenPayload(tokenInfo);
        return tokenInfo;
    }

    public TokenValidationResponse validateAndGetTokenInfoWithWriteAccess(String authHeader) {
        TokenValidationResponse tokenInfo = validateAndGetTokenInfo(authHeader);
        validationUtils.validateRoleCanWrite(tokenInfo.getRole());
        return tokenInfo;
    }

    public IncomeResponseDTO toResponseDTOWithFetch(Income income, TokenValidationResponse tokenInfo) {
        return responseBuilder.buildResponse(income, tokenInfo.getUsername());
    }

    public IncomeResponseDTO toResponseDTOWithUsername(Income income, String username) {
        return responseBuilder.buildResponse(income, username);
    }

    public List<IncomeResponseDTO> toResponseDTOList(List<Income> incomes, TokenValidationResponse currentTokenInfo) {
        // All incomes on the same farm use the current token's username for own records,
        // fallback to userId string for records created by other farm members
        return incomes.stream()
                .map(income -> {
                    String username;
                    if (income.getUserId() != null
                            && income.getUserId().equals(currentTokenInfo.getUserId())) {
                        username = currentTokenInfo.getUsername();
                    } else {
                        // No HTTP call — use userId as display fallback for other farm members
                        username = income.getUserId() != null
                                ? income.getUserId().toString()
                                : "Unknown";
                    }
                    return toResponseDTOWithUsername(income, username);
                })
                .toList();
    }

    public Page<IncomeResponseDTO> toResponseDTOPage(Page<Income> incomes, TokenValidationResponse currentTokenInfo) {
        List<IncomeResponseDTO> list = toResponseDTOList(incomes.getContent(), currentTokenInfo);
        return incomes.map(income -> {
            int index = incomes.getContent().indexOf(income);
            return list.get(index);
        });
    }

    public void checkOwnershipOrMaster(Income income, UUID userId, String role) {
        if (!income.getUserId().equals(userId) && !"MASTER".equals(role)) {
            throw new AccessDeniedException("You can only modify your own incomes");
        }
    }

    public void checkFarmAccess(UUID requestedFarmId, UUID tokenFarmId, String role) {
        if (!tokenFarmId.equals(requestedFarmId) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("Cannot view incomes of another farm");
        }
    }

    public Income findIncomeByIdAndFarmId(Long id, UUID farmId) {
        return incomeRepository.findByIdAndFarmId(id, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found with id: " + id));
    }
}