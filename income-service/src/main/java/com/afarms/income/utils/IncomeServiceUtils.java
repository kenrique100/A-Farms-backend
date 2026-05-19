package com.afarms.income.utils;

import com.afarms.income.client.UserServiceClient;
import com.afarms.income.exception.AccessDeniedException;
import com.afarms.income.exception.ResourceNotFoundException;
import com.afarms.income.model.dto.IncomeResponseDTO;
import com.afarms.income.model.dto.TokenValidationResponse;
import com.afarms.income.model.dto.UserDetailsResponse;
import com.afarms.income.model.entity.Income;
import com.afarms.income.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceUtils {

    private final UserServiceClient userServiceClient;
    private final IncomeValidationUtils validationUtils;
    private final ResponseBuilderUtils responseBuilder;
    private final IncomeRepository incomeRepository;

    public TokenValidationResponse validateAndGetTokenInfo(String authHeader) {
        validationUtils.validateAuthorizationHeader(authHeader);
        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        validationUtils.validateTokenPayload(tokenInfo);
        return tokenInfo;
    }

    public TokenValidationResponse validateAndGetTokenInfoWithWriteAccess(String authHeader) {
        TokenValidationResponse tokenInfo = validateAndGetTokenInfo(authHeader);
        validationUtils.validateRoleCanWrite(tokenInfo.getRole());
        return tokenInfo;
    }

    public String getUsernameWithCache(Map<UUID, String> cache, UUID userId, String authHeader) {
        return cache.computeIfAbsent(userId, uid -> {
            UserDetailsResponse userDetails = userServiceClient.getUserById(uid, authHeader);
            return userDetails.getUsername();
        });
    }

    public Map<UUID, String> createUserNameCache() {
        return new ConcurrentHashMap<>();
    }

    public IncomeResponseDTO toResponseDTOWithUsername(Income income, String username) {
        return responseBuilder.buildResponse(income, username);
    }

    public IncomeResponseDTO toResponseDTOWithFetch(Income income, String authHeader) {
        UserDetailsResponse userDetails = userServiceClient.getUserById(income.getUserId(), authHeader);
        return responseBuilder.buildResponse(income, userDetails.getUsername());
    }

    public List<IncomeResponseDTO> toResponseDTOList(List<Income> incomes, String authHeader) {
        Map<UUID, String> userNameCache = createUserNameCache();
        return incomes.stream()
                .map(income -> {
                    String username = getUsernameWithCache(userNameCache, income.getUserId(), authHeader);
                    return toResponseDTOWithUsername(income, username);
                })
                .toList();
    }

    public Page<IncomeResponseDTO> toResponseDTOPage(Page<Income> incomes, String authHeader) {
        Map<UUID, String> userNameCache = createUserNameCache();
        return incomes.map(income -> {
            String username = getUsernameWithCache(userNameCache, income.getUserId(), authHeader);
            return toResponseDTOWithUsername(income, username);
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