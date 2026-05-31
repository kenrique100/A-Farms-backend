package com.afarms.investment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    private UUID userId;
    private String username;
    private String email;
    private Boolean isValid;
    private String role;
    private UUID farmId;
}