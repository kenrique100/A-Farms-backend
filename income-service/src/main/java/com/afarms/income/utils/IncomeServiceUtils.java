package com.afarms.income.utils;

import com.afarms.income.client.UserServiceClient;
import com.afarms.income.exception.AccessDeniedException;
import com.afarms.income.exception.ResourceNotFoundException;
import com.afarms.income.model.dto.FarmUserDTO;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceUtils {

    private final JwtUtil jwtUtil;
    private final IncomeValidationUtils validationUtils;
    private final ResponseBuilderUtils responseBuilder;
    private final IncomeRepository incomeRepository;
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

    /**
     * Builds a map of userId → username for all users of the given farm.
     * Uses UserServiceClient with fallback strategy.
     */
    public Map<UUID, String> getUsernameMapForFarm(String authHeader, UUID farmId) {
        Map<UUID, String> map = new HashMap<>();

        try {
            List<FarmUserDTO> farmUsers = userServiceClient.getFarmUsers(authHeader, farmId);
            if (farmUsers != null && !farmUsers.isEmpty()) {
                for (FarmUserDTO user : farmUsers) {
                    if (user.getId() != null) {
                        // Prefer username, fallback to email
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

    /**
     * Resolve username for a single user with multiple fallback strategies
     */
    private String resolveUsername(String authHeader, UUID userId,
                                   TokenValidationResponse tokenInfo,
                                   Map<UUID, String> usernameMap) {
        // Strategy 1: Check if it's the current user
        if (userId.equals(tokenInfo.getUserId())) {
            return tokenInfo.getUsername();
        }

        // Strategy 2: Check the provided map
        if (usernameMap != null && usernameMap.containsKey(userId)) {
            String username = usernameMap.get(userId);
            if (username != null && !username.isBlank() && !username.equals(userId.toString())) {
                return username;
            }
        }

        // Strategy 3: Try direct fetch from user service
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

        // Strategy 4: Fallback to showing "User" + short ID
        String shortId = userId.toString().substring(0, Math.min(8, userId.toString().length()));
        log.debug("Using fallback username for user {}: User-{}", userId, shortId);
        return "User-" + shortId;
    }

    public IncomeResponseDTO toResponseDTOWithFetch(Income income, TokenValidationResponse tokenInfo,
                                                    Map<UUID, String> usernameMap) {
        String username = resolveUsername(null, income.getUserId(), tokenInfo, usernameMap);
        return responseBuilder.buildResponse(income, username);
    }

    // Overloaded method for when authHeader is available
    public IncomeResponseDTO toResponseDTOWithFetch(String authHeader, Income income,
                                                    TokenValidationResponse tokenInfo,
                                                    Map<UUID, String> usernameMap) {
        String username = resolveUsername(authHeader, income.getUserId(), tokenInfo, usernameMap);
        return responseBuilder.buildResponse(income, username);
    }

    public List<IncomeResponseDTO> toResponseDTOList(List<Income> incomes,
                                                     TokenValidationResponse currentTokenInfo,
                                                     Map<UUID, String> usernameMap) {
        return incomes.stream()
                .map(income -> toResponseDTOWithFetch(null, income, currentTokenInfo, usernameMap))
                .collect(Collectors.toList());
    }

    public Page<IncomeResponseDTO> toResponseDTOPage(Page<Income> incomes,
                                                     TokenValidationResponse currentTokenInfo,
                                                     Map<UUID, String> usernameMap) {
        return incomes.map(income -> toResponseDTOWithFetch(null, income, currentTokenInfo, usernameMap));
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