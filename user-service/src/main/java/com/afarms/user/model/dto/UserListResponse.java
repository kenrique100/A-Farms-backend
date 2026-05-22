package com.afarms.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListResponse {
    private UUID userId;
    private String email;
    private String username;
    private String role;
    private UUID farmId;
}