package com.afarms.transaction.model.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionSummaryDTO {
    private long totalCount;
    private BigDecimal totalAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal totalInvestment;
}
