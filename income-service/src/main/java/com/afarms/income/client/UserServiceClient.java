package com.afarms.income.client;

import com.afarms.income.exception.ExternalServiceException;
import com.afarms.income.exception.UnauthorizedException;
import com.afarms.income.model.dto.TokenValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(RestClient.Builder restClientBuilder,
                             @Value("${services.user.url:http://localhost:8085}") String userServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(userServiceUrl).build();
    }

    public TokenValidationResponse validateToken(String authHeader) {
        try {
            TokenValidationResponse response = restClient.post()
                    .uri("/api/v1/users/validate")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new UnauthorizedException("Unauthorized access token");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new ExternalServiceException("User-service is currently unavailable");
                    })
                    .body(TokenValidationResponse.class);

            if (response == null) {
                throw new ExternalServiceException("User-service returned empty token validation response");
            }
            return response;
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Failed to communicate with user-service", ex);
        }
    }
}
