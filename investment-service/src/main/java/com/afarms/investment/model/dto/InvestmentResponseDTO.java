package com.afarms.investment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentResponseDTO {
    private Long id;
    private BigDecimal initialAmount;
    private BigDecimal currentBalance;
    private BigDecimal roi;
    private UUID farmId;
    private UUID userId;
    private String userName;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private LocalDateTime createdTimestamp;
}