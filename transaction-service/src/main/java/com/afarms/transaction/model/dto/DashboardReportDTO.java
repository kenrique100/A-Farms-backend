package com.afarms.transaction.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardReportDTO {
    private List<TransactionDTO> transactions;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal totalInvestment;
    private BigDecimal netGain;
    private BigDecimal netLoss;
}