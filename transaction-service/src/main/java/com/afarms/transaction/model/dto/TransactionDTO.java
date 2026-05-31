package com.afarms.transaction.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private String type;
    private Long referenceId;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private String createdBy;
    private String description;
}