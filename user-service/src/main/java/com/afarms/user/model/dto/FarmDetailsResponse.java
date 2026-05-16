package com.afarms.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmDetailsResponse {
    private UUID farmId;
    private String farmName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private MasterDetails master;
    private List<SubUserDetails> subUsers;
    private Integer totalUsers;
    private Integer totalSubUsers;
}