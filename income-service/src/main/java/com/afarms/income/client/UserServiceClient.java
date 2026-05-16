package com.afarms.income.client;

import com.afarms.income.exception.ExternalServiceException;
import com.afarms.income.exception.UnauthorizedException;
import com.afarms.income.model.dto.TokenValidationResponse;
import com.afarms.income.model.dto.UserDetailsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(RestClient.Builder restClientBuilder,
                             @Value("${services.user.url}") String userServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(userServiceUrl).build();
    }

    public TokenValidationResponse validateToken(String authHeader) {
        try {
            TokenValidationResponse response = restClient.post()
                    .uri("/api/v1/users/validate")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new UnauthorizedException("Invalid or expired token");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new ExternalServiceException("User service unavailable");
                    })
                    .body(TokenValidationResponse.class);

            if (response == null || response.getUserId() == null) {
                throw new ExternalServiceException("Invalid token validation response");
            }
            return response;
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Failed to communicate with user service", ex);
        }
    }

    public UserDetailsResponse getUserById(UUID userId, String authHeader) {
        try {
            UserDetailsResponse response = restClient.get()
                    .uri("/api/v1/users/{userId}", userId)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new ExternalServiceException("User not found or access denied");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new ExternalServiceException("User service unavailable");
                    })
                    .body(UserDetailsResponse.class);

            if (response == null) {
                throw new ExternalServiceException("Empty user details response");
            }
            return response;
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Failed to fetch user details", ex);
        }
    }
}