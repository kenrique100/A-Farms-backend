package com.afarms.transaction.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentTransactionDTO {
    private Long id;
    private String description;
    private BigDecimal initialAmount;
    private BigDecimal currentBalance;
    private String createdBy;
    private LocalDate createdAt;
}