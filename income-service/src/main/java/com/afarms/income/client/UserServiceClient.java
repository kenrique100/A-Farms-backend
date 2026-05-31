package com.afarms.income.client;

import com.afarms.income.exception.ExternalServiceException;
import com.afarms.income.model.dto.FarmUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(RestClient.Builder restClientBuilder,
                             @Value("${services.user.url}") String userServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(userServiceUrl).build();
    }

    @Cacheable(value = "farmUsers", key = "#farmId")
    public List<FarmUserDTO> getFarmUsers(String authHeader, UUID farmId) {
        try {
            List<FarmUserDTO> users = restClient.get()
                    .uri("/api/v1/users/farm/users")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new ExternalServiceException("User service 4xx fetching farm users: "
                                + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new ExternalServiceException("User service 5xx fetching farm users");
                    })
                    .body(new ParameterizedTypeReference<>() {});

            if (users != null && !users.isEmpty()) {
                log.debug("Fetched {} users for farm {}", users.size(), farmId);
                return users;
            }
            log.warn("No users returned for farm {}", farmId);
            return Collections.emptyList();

        } catch (ExternalServiceException e) {
            log.error("Failed to fetch farm users for farm {}: {}", farmId, e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error fetching farm users for farm {}: {}", farmId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Cacheable(value = "userById", key = "#userId")
    public FarmUserDTO getUserById(String authHeader, UUID userId) {
        try {
            FarmUserDTO user = restClient.get()
                    .uri("/api/v1/users/{userId}", userId)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new ExternalServiceException("User service 4xx for user "
                                + userId + ": " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new ExternalServiceException("User service 5xx for user " + userId);
                    })
                    .body(FarmUserDTO.class);

            if (user != null && user.getUsername() != null && !user.getUsername().isBlank()) {
                log.debug("Fetched user: {}", user.getUsername());
                return user;
            }
            log.warn("Incomplete user response for id {}", userId);
            return null;

        } catch (ExternalServiceException e) {
            log.error("Failed to fetch user {}: {}", userId, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    // Runs every 10 minutes — evicts stale cache entries automatically
    @Scheduled(fixedRateString = "${cache.user.evict-rate-ms:600000}")
    @CacheEvict(value = {"farmUsers", "userById"}, allEntries = true)
    public void evictUserCaches() {
        log.debug("Scheduled eviction of farmUsers and userById caches");
    }
}