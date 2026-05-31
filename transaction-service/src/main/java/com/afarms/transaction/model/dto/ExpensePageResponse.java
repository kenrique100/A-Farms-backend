package com.afarms.transaction.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExpensePageResponse {
    private List<ExpenseTransactionDTO> content;
}