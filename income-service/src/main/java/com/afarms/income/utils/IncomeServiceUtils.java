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
        String email = claims.get("email", String.class);

        if (userIdStr == null || farmIdStr == null) {
            throw new AccessDeniedException("Invalid token payload");
        }

        TokenValidationResponse tokenInfo = new TokenValidationResponse(
                UUID.fromString(userIdStr),
                username,
                email != null ? email : username,
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
                        String displayName = (user.getUsername() != null && !user.getUsername().isBlank())
                                ? user.getUsername()
                                : (user.getEmail() != null ? user.getEmail() : user.getId().toString());
                        map.put(user.getId(), displayName);
                    }
                }
                log.debug("Resolved {} usernames for farm {}", map.size(), farmId);
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
            String name = usernameMap.get(userId);
            if (name != null && !name.isBlank() && !name.equals(userId.toString())) {
                return name;
            }
        }

        if (authHeader != null) {
            try {
                FarmUserDTO user = userServiceClient.getUserById(authHeader, userId);
                if (user != null) {
                    String displayName = (user.getUsername() != null && !user.getUsername().isBlank())
                            ? user.getUsername() : user.getEmail();
                    if (displayName != null && !displayName.isBlank()) {
                        return displayName;
                    }
                }
            } catch (Exception e) {
                log.debug("Direct user fetch failed for {}: {}", userId, e.getMessage());
            }
        }

        return "User-" + userId.toString().substring(0, 8);
    }

    // Single authoritative method — always pass authHeader
    public IncomeResponseDTO toResponseDTOWithFetch(String authHeader, Income income,
                                                    TokenValidationResponse tokenInfo,
                                                    Map<UUID, String> usernameMap) {
        String username = resolveUsername(authHeader, income.getUserId(), tokenInfo, usernameMap);
        return responseBuilder.buildResponse(income, username);
    }

    public List<IncomeResponseDTO> toResponseDTOList(String authHeader, List<Income> incomes,
                                                     TokenValidationResponse tokenInfo,
                                                     Map<UUID, String> usernameMap) {
        return incomes.stream()
                .map(income -> toResponseDTOWithFetch(authHeader, income, tokenInfo, usernameMap))
                .collect(Collectors.toList());
    }

    public Page<IncomeResponseDTO> toResponseDTOPage(String authHeader, Page<Income> incomes,
                                                     TokenValidationResponse tokenInfo,
                                                     Map<UUID, String> usernameMap) {
        return incomes.map(income -> toResponseDTOWithFetch(authHeader, income, tokenInfo, usernameMap));
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