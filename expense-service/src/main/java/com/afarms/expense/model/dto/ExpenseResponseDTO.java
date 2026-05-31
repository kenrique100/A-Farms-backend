package com.afarms.expense.model.dto;

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
public class ExpenseResponseDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate occurredAt;
    private UUID farmId;
    private UUID userId;
    private String userName;
    private Long transactionId;
    private LocalDateTime createdAt;
}