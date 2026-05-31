package com.afarms.expense.utils;

import com.afarms.expense.model.dto.ExpenseRequestDTO;
import com.afarms.expense.model.dto.TokenValidationResponse;
import com.afarms.expense.model.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseBuilderUtils {

    public Expense buildExpenseFromRequest(ExpenseRequestDTO request, TokenValidationResponse tokenInfo) {
        return Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .occurredAt(request.getOccurredAt())
                .farmId(tokenInfo.getFarmId())
                .userId(tokenInfo.getUserId())
                .build();
    }

    public void updateExpenseFromRequest(Expense expense, ExpenseRequestDTO request) {
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setOccurredAt(request.getOccurredAt());
    }
}