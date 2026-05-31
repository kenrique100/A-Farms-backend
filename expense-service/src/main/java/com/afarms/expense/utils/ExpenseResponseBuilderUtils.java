package com.afarms.expense.utils;

import com.afarms.expense.model.dto.ExpenseResponseDTO;
import com.afarms.expense.model.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseResponseBuilderUtils {

    public ExpenseResponseDTO buildResponse(Expense expense, String userName) {
        String displayName = (userName != null && !userName.isBlank())
                ? userName
                : (expense.getUserId() != null ? expense.getUserId().toString() : "Unknown");

        return ExpenseResponseDTO.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .occurredAt(expense.getOccurredAt())
                .farmId(expense.getFarmId())
                .userId(expense.getUserId())
                .userName(displayName)
                .createdAt(expense.getCreatedAt())
                .build();
    }
}