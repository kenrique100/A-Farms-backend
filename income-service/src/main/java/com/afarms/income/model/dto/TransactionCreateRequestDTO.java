package com.afarms.income.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreateRequestDTO {
    private Long incomeId;
    private UUID farmId;
    private UUID userId;
    private String description;
    private BigDecimal amount;
    private LocalDate occurredAt;
}