package com.afarms.investment.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentRequestDTO {

    @NotNull(message = "Initial amount is required")
    @DecimalMin(value = "1.00", message = "Initial amount must be at least 1.00")
    private BigDecimal initialAmount;

    @NotNull(message = "Current balance is required")
    @DecimalMin(value = "0.00", message = "Current balance cannot be negative")
    private BigDecimal currentBalance;
}