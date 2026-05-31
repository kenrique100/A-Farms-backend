package com.afarms.expense.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmUserDTO {
    private UUID id;
    private String email;
    private String username;
    private String role;
    private UUID farmId;
}