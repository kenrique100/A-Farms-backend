package com.afarms.investment.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TransactionServiceClient {

    private final RestClient.Builder restClientBuilder;

    public TransactionServiceClient(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    public RestClient.Builder getRestClientBuilder() {
        return restClientBuilder;
    }
}
