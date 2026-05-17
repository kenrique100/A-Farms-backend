package com.afarms.income.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    private UUID userId;
    private UUID farmId;
    private String role;
    private String username;
}