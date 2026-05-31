package com.afarms.transaction.model.dto;

import java.util.UUID;

public class TokenValidationResponse {
    private final UUID userId;
    private final String username;
    private final String role;
    private final UUID farmId;

    public TokenValidationResponse(UUID userId, String username, String role, UUID farmId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.farmId = farmId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public UUID getFarmId() {
        return farmId;
    }
}
