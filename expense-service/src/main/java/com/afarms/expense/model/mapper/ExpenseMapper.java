package com.afarms.expense.model.mapper;

import com.afarms.expense.model.dto.ExpenseRequestDTO;
import com.afarms.expense.model.dto.ExpenseResponseDTO;
import com.afarms.expense.model.entity.Expense;

public final class ExpenseMapper {

    private ExpenseMapper() {
    }

    public static Expense toEntity(ExpenseRequestDTO request) {
        Expense entity = new Expense();
        updateEntity(entity, request);
        return entity;
    }

    public static void updateEntity(Expense entity, ExpenseRequestDTO request) {
        entity.setDescription(request.getDescription());
        entity.setAmount(request.getAmount());
        entity.setOccurredAt(request.getOccurredAt());
    }

    public static ExpenseResponseDTO toResponse(Expense entity) {
        ExpenseResponseDTO response = new ExpenseResponseDTO();
        response.setId(entity.getId());
        response.setDescription(entity.getDescription());
        response.setAmount(entity.getAmount());
        response.setOccurredAt(entity.getOccurredAt());
        response.setFarmId(entity.getFarmId());
        response.setUserId(entity.getUserId());
        response.setTransactionId(entity.getTransactionId());
        return response;
    }
}
