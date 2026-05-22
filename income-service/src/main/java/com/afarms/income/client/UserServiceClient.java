package com.afarms.income.client;

import com.afarms.income.model.dto.FarmUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class UserServiceClient {

    private final RestClient restClient;
    private final Map<String, List<FarmUserDTO>> farmUserCache = new ConcurrentHashMap<>();
    private final Map<String, FarmUserDTO> userCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 300000; // 5 minutes

    public UserServiceClient(RestClient.Builder restClientBuilder,
                             @Value("${services.user.url}") String userServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(userServiceUrl).build();
    }

    /**
     * Fetch all users belonging to a farm (accessible by MASTER, ADMIN, and SUB_USER).
     * Returns empty list on failure and caches results.
     */
    public List<FarmUserDTO> getFarmUsers(String authHeader, UUID farmId) {
        String cacheKey = farmId.toString();

        // Check cache with TTL
        if (isCacheValid(cacheKey)) {
            log.debug("Returning cached farm users for farm {}", farmId);
            return farmUserCache.get(cacheKey);
        }

        try {
            List<FarmUserDTO> users = restClient.get()
                    .uri("/api/v1/users/farm/users")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.warn("User service 4xx while fetching farm {} users, status: {}", farmId, res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("User service 5xx while fetching farm {} users", farmId);
                    })
                    .body(new ParameterizedTypeReference<>() {});

            if (users != null && !users.isEmpty()) {
                // Cache the result
                farmUserCache.put(cacheKey, users);
                cacheTimestamps.put(cacheKey, System.currentTimeMillis());

                // Also cache individual users
                for (FarmUserDTO user : users) {
                    if (user.getId() != null) {
                        userCache.put(user.getId().toString(), user);
                        cacheTimestamps.put(user.getId().toString(), System.currentTimeMillis());
                    }
                }
                log.debug("Fetched and cached {} users for farm {}", users.size(), farmId);
                return users;
            }

            log.warn("No users found or null response for farm {}", farmId);
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to fetch farm users from user service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetch a single user by ID. Returns null on failure.
     */
    public FarmUserDTO getUserById(String authHeader, UUID userId) {
        String cacheKey = userId.toString();

        // Check cache with TTL
        if (isCacheValid(cacheKey)) {
            log.debug("Returning cached user for id {}", userId);
            return userCache.get(cacheKey);
        }

        try {
            FarmUserDTO user = restClient.get()
                    .uri("/api/v1/users/{userId}", userId)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.warn("User service 4xx for user id {}, status: {}", userId, res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("User service 5xx for user id {}", userId);
                    })
                    .body(FarmUserDTO.class);

            if (user != null && user.getUsername() != null && !user.getUsername().isBlank()) {
                userCache.put(cacheKey, user);
                cacheTimestamps.put(cacheKey, System.currentTimeMillis());
                log.debug("Fetched and cached user: {}", user.getUsername());
                return user;
            }

            log.warn("User not found or null response for id {}", userId);
            return null;

        } catch (Exception e) {
            log.error("Failed to fetch user {} from user service: {}", userId, e.getMessage());
            return null;
        }
    }

    private boolean isCacheValid(String key) {
        if (!farmUserCache.containsKey(key) && !userCache.containsKey(key)) {
            return false;
        }
        Long timestamp = cacheTimestamps.get(key);
        if (timestamp == null) {
            return false;
        }
        return (System.currentTimeMillis() - timestamp) < CACHE_TTL_MS;
    }

    /**
     * Clear cache (useful for testing or when users are updated)
     */
    public void clearCache() {
        farmUserCache.clear();
        userCache.clear();
        cacheTimestamps.clear();
        log.debug("User service cache cleared");
    }
}