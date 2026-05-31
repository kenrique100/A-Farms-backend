package com.afarms.transaction.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeTransactionDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate occurredAt;
    private String userName;
}