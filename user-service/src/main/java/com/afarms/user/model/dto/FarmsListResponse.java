package com.afarms.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmsListResponse {
    private List<FarmDetailsResponse> farms;
    private Integer totalFarms;
    private Integer totalUsers;
    private Integer totalMasters;
    private Integer totalSubUsers;
}