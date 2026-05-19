package com.afarms.transaction.client;

import com.afarms.transaction.exception.ExternalServiceException;
import com.afarms.transaction.exception.UnauthorizedException;
import com.afarms.transaction.model.dto.TokenValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(RestClient.Builder restClientBuilder,
                             @Value("${services.user.url}") String userServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(userServiceUrl).build();
    }

    public TokenValidationResponse validateToken(String authHeader) {
        try {
            return restClient.post()
                    .uri("/api/v1/users/validate")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new UnauthorizedException("Invalid or expired token");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new ExternalServiceException("User service unavailable");
                    })
                    .body(TokenValidationResponse.class);
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to communicate with user service: " + ex.getMessage(), ex);
        }
    }
}