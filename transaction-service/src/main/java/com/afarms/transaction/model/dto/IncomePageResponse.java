package com.afarms.transaction.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class IncomePageResponse {
    private List<IncomeTransactionDTO> content;
}