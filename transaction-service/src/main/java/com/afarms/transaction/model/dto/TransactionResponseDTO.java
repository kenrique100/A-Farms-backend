package com.afarms.transaction.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionResponseDTO {
    private Long id;
    private String type;
    private Long referenceId;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private String createdBy;
    private UUID farmId;
    private UUID userId;
    private LocalDateTime createdAt;
}
