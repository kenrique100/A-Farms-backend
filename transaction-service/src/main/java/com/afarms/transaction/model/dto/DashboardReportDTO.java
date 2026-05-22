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
    private List<IncomeTransactionDTO> transactions;
    private BigDecimal totalIncome;
    private Long totalCount;
    private BigDecimal averageIncome;
    private BigDecimal minIncome;
    private BigDecimal maxIncome;
}