package com.afarms.expense.utils;

import com.afarms.expense.client.UserServiceClient;
import com.afarms.expense.exception.AccessDeniedException;
import com.afarms.expense.exception.ResourceNotFoundException;
import com.afarms.expense.model.dto.FarmUserDTO;
import com.afarms.expense.model.dto.ExpenseResponseDTO;
import com.afarms.expense.model.dto.TokenValidationResponse;
import com.afarms.expense.model.entity.Expense;
import com.afarms.expense.repository.ExpenseRepository;
import com.afarms.expense.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceUtils {

    private final JwtUtil jwtUtil;
    private final ExpenseValidationUtils validationUtils;
    private final ExpenseResponseBuilderUtils responseBuilder;
    private final ExpenseRepository expenseRepository;
    private final UserServiceClient userServiceClient;

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

        if (userIdStr == null || farmIdStr == null) {
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

    public Map<UUID, String> getUsernameMapForFarm(String authHeader, UUID farmId) {
        Map<UUID, String> map = new HashMap<>();

        try {
            List<FarmUserDTO> farmUsers = userServiceClient.getFarmUsers(authHeader, farmId);
            if (farmUsers != null && !farmUsers.isEmpty()) {
                for (FarmUserDTO user : farmUsers) {
                    if (user.getId() != null) {
                        String displayName = user.getUsername() != null && !user.getUsername().isBlank()
                                ? user.getUsername()
                                : (user.getEmail() != null ? user.getEmail() : user.getId().toString());
                        map.put(user.getId(), displayName);
                    }
                }
                log.debug("Resolved {} usernames for farm {}", map.size(), farmId);
            } else {
                log.warn("No farm users returned from user service for farm {}", farmId);
            }
        } catch (Exception e) {
            log.error("Failed to resolve usernames for farm {}: {}", farmId, e.getMessage());
        }

        return map;
    }

    private String resolveUsername(String authHeader, UUID userId,
                                   TokenValidationResponse tokenInfo,
                                   Map<UUID, String> usernameMap) {
        if (userId.equals(tokenInfo.getUserId())) {
            return tokenInfo.getUsername();
        }

        if (usernameMap != null && usernameMap.containsKey(userId)) {
            String username = usernameMap.get(userId);
            if (username != null && !username.isBlank() && !username.equals(userId.toString())) {
                return username;
            }
        }

        try {
            FarmUserDTO user = userServiceClient.getUserById(authHeader, userId);
            if (user != null) {
                String displayName = user.getUsername() != null && !user.getUsername().isBlank()
                        ? user.getUsername()
                        : (user.getEmail() != null ? user.getEmail() : null);
                if (displayName != null && !displayName.isBlank()) {
                    log.debug("Direct fetch resolved username '{}' for user {}", displayName, userId);
                    return displayName;
                }
            }
        } catch (Exception e) {
            log.debug("Direct fetch failed for user {}: {}", userId, e.getMessage());
        }

        String shortId = userId.toString().substring(0, Math.min(8, userId.toString().length()));
        log.debug("Using fallback username for user {}: User-{}", userId, shortId);
        return "User-" + shortId;
    }

    public ExpenseResponseDTO toResponseDTOWithFetch(Expense expense, TokenValidationResponse tokenInfo,
                                                     Map<UUID, String> usernameMap) {
        String username = resolveUsername(null, expense.getUserId(), tokenInfo, usernameMap);
        return responseBuilder.buildResponse(expense, username);
    }

    public ExpenseResponseDTO toResponseDTOWithFetch(String authHeader, Expense expense,
                                                     TokenValidationResponse tokenInfo,
                                                     Map<UUID, String> usernameMap) {
        String username = resolveUsername(authHeader, expense.getUserId(), tokenInfo, usernameMap);
        return responseBuilder.buildResponse(expense, username);
    }

    public List<ExpenseResponseDTO> toResponseDTOList(List<Expense> expenses,
                                                      TokenValidationResponse currentTokenInfo,
                                                      Map<UUID, String> usernameMap) {
        return expenses.stream()
                .map(expense -> toResponseDTOWithFetch(null, expense, currentTokenInfo, usernameMap))
                .collect(Collectors.toList());
    }

    public Page<ExpenseResponseDTO> toResponseDTOPage(Page<Expense> expenses,
                                                      TokenValidationResponse currentTokenInfo,
                                                      Map<UUID, String> usernameMap) {
        return expenses.map(expense -> toResponseDTOWithFetch(null, expense, currentTokenInfo, usernameMap));
    }

    public void checkOwnershipOrMaster(Expense expense, UUID userId, String role) {
        if (!expense.getUserId().equals(userId) && !"MASTER".equals(role)) {
            throw new AccessDeniedException("You can only modify your own expenses");
        }
    }

    public void checkFarmAccess(UUID requestedFarmId, UUID tokenFarmId, String role) {
        if (!tokenFarmId.equals(requestedFarmId) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("Cannot view expenses of another farm");
        }
    }

    public Expense findExpenseByIdAndFarmId(Long id, UUID farmId) {
        return expenseRepository.findByIdAndFarmId(id, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }
}