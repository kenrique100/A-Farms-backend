package com.afarms.income.model.mapper;

import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.IncomeResponseDTO;
import com.afarms.income.model.entity.Income;

public final class IncomeMapper {

    private IncomeMapper() {
    }

    public static Income toEntity(IncomeRequestDTO request) {
        Income entity = new Income();
        entity.setDescription(request.getDescription());
        entity.setAmount(request.getAmount());
        entity.setOccurredAt(request.getOccurredAt());
        return entity;
    }

    public static IncomeResponseDTO toResponse(Income entity) {
        IncomeResponseDTO response = new IncomeResponseDTO();
        response.setId(entity.getId());
        response.setDescription(entity.getDescription());
        response.setAmount(entity.getAmount());
        response.setOccurredAt(entity.getOccurredAt());
        return response;
    }
}
