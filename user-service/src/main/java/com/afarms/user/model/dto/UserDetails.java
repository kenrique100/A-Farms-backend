package com.afarms.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserDetails {
    private String email;
    private String role;
    private UUID farmId;
}