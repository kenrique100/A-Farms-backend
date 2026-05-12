package com.afarms.expense.model.mapper;

import com.afarms.expense.model.dto.ExpenseRequestDTO;
import com.afarms.expense.model.dto.ExpenseResponseDTO;
import com.afarms.expense.model.entity.Expense;

public final class ExpenseMapper {

    private ExpenseMapper() {
    }

    public static Expense toEntity(ExpenseRequestDTO request) {
        Expense entity = new Expense();
        entity.setDescription(request.getDescription());
        entity.setAmount(request.getAmount());
        entity.setOccurredAt(request.getOccurredAt());
        return entity;
    }

    public static ExpenseResponseDTO toResponse(Expense entity) {
        ExpenseResponseDTO response = new ExpenseResponseDTO();
        response.setId(entity.getId());
        response.setDescription(entity.getDescription());
        response.setAmount(entity.getAmount());
        response.setOccurredAt(entity.getOccurredAt());
        return response;
    }
}
