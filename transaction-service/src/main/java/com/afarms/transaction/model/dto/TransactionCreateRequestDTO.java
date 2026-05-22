package com.afarms.transaction.model.dto;

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
    private String type;
    private Long referenceId;
    private LocalDate date;
    private BigDecimal amount;
    private String createdBy;
    private UUID farmId;
    private UUID userId;
}