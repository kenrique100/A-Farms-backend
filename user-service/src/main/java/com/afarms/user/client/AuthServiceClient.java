package com.afarms.user.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthServiceClient {

    private final RestClient.Builder restClientBuilder;

    public AuthServiceClient(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    public RestClient.Builder getRestClientBuilder() {
        return restClientBuilder;
    }
}
