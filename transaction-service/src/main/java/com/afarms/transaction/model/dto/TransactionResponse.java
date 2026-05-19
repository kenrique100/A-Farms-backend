package com.afarms.transaction.model.dto;

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
public class TransactionResponse {
    private Long id;
    private Long incomeId;
    private Long expenseId;
    private Long investmentId;
    private UUID farmId;
    private UUID userId;
    private String description;
    private BigDecimal amount;
    private LocalDate occurredAt;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}