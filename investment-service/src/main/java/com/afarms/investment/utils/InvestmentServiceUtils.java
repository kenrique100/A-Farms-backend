package com.afarms.investment.utils;

import com.afarms.investment.client.UserServiceClient;
import com.afarms.investment.exception.AccessDeniedException;
import com.afarms.investment.exception.ResourceNotFoundException;
import com.afarms.investment.model.dto.FarmUserDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.model.dto.TokenValidationResponse;
import com.afarms.investment.model.entity.Investment;
import com.afarms.investment.repository.InvestmentRepository;
import com.afarms.investment.security.JwtUtil;
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
public class InvestmentServiceUtils {

    private final JwtUtil jwtUtil;
    private final InvestmentValidationUtils validationUtils;
    private final ResponseBuilderUtils responseBuilder;
    private final InvestmentRepository investmentRepository;
    private final UserServiceClient userServiceClient;

    public TokenValidationResponse validateAndGetTokenInfo(String authHeader) {
        log.debug("Validating token");
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
            log.warn("Token is missing userId or farmId claims");
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
        log.debug("Token validated for user={} farm={} role={}", username, farmIdStr, role);
        return tokenInfo;
    }

    public TokenValidationResponse validateAndGetTokenInfoWithWriteAccess(String authHeader) {
        TokenValidationResponse tokenInfo = validateAndGetTokenInfo(authHeader);
        validationUtils.validateRoleCanWrite(tokenInfo.getRole());
        log.debug("Write access granted for role={}", tokenInfo.getRole());
        return tokenInfo;
    }

    public Map<UUID, String> getUsernameMapForFarm(String authHeader, UUID farmId) {
        Map<UUID, String> map = new HashMap<>();
        log.debug("Resolving username map for farm: {}", farmId);
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
            } else {
                log.warn("No farm users returned for farm {}", farmId);
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
            log.debug("Username resolved from token for userId={}", userId);
            return tokenInfo.getUsername();
        }

        if (usernameMap != null && usernameMap.containsKey(userId)) {
            String name = usernameMap.get(userId);
            if (name != null && !name.isBlank() && !name.equals(userId.toString())) {
                log.debug("Username resolved from map for userId={}", userId);
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
                        log.debug("Username resolved via direct fetch for userId={}", userId);
                        return displayName;
                    }
                }
            } catch (Exception e) {
                log.debug("Direct user fetch failed for userId={}: {}", userId, e.getMessage());
            }
        }

        String fallback = "User-" + userId.toString().substring(0, 8);
        log.debug("Using fallback username '{}' for userId={}", fallback, userId);
        return fallback;
    }

    public InvestmentResponseDTO toResponseDTO(String authHeader, Investment investment,
                                               TokenValidationResponse tokenInfo,
                                               Map<UUID, String> usernameMap) {
        String username = resolveUsername(authHeader, investment.getUserId(), tokenInfo, usernameMap);
        return responseBuilder.buildResponse(investment, username);
    }

    public List<InvestmentResponseDTO> toResponseDTOList(String authHeader, List<Investment> investments,
                                                         TokenValidationResponse tokenInfo,
                                                         Map<UUID, String> usernameMap) {
        return investments.stream()
                .map(inv -> toResponseDTO(authHeader, inv, tokenInfo, usernameMap))
                .collect(Collectors.toList());
    }

    public Page<InvestmentResponseDTO> toResponseDTOPage(String authHeader, Page<Investment> investments,
                                                         TokenValidationResponse tokenInfo,
                                                         Map<UUID, String> usernameMap) {
        return investments.map(inv -> toResponseDTO(authHeader, inv, tokenInfo, usernameMap));
    }

    public void checkOwnershipOrMaster(Investment investment, UUID userId, String role) {
        if (!investment.getUserId().equals(userId) && !"MASTER".equals(role)) {
            log.warn("Unauthorized modification attempt on investment id={} by userId={}",
                    investment.getId(), userId);
            throw new AccessDeniedException("You can only modify your own investments");
        }
    }

    public void checkFarmAccess(UUID requestedFarmId, UUID tokenFarmId, String role) {
        if (!tokenFarmId.equals(requestedFarmId) && !"ADMIN".equals(role)) {
            log.warn("Cross-farm access attempt: requested={} token={} role={}",
                    requestedFarmId, tokenFarmId, role);
            throw new AccessDeniedException("Cannot view investments of another farm");
        }
    }

    public Investment findByIdAndFarmId(Long id, UUID farmId) {
        return investmentRepository.findByIdAndFarmId(id, farmId)
                .orElseThrow(() -> {
                    log.warn("Investment not found: id={} farmId={}", id, farmId);
                    return new ResourceNotFoundException("Investment not found with id: " + id);
                });
    }
}