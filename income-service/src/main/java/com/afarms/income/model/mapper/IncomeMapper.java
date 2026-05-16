package com.afarms.income.model.mapper;

import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.IncomeResponseDTO;
import com.afarms.income.model.entity.Income;
import java.util.UUID;

public final class IncomeMapper {

    private IncomeMapper() {
    }

    public static Income toEntity(IncomeRequestDTO request, UUID farmId, UUID userId) {
        Income entity = new Income();
        entity.setDescription(request.getDescription());
        entity.setAmount(request.getAmount());
        entity.setOccurredAt(request.getOccurredAt());
        entity.setFarmId(farmId);
        entity.setUserId(userId);
        return entity;
    }

    public static IncomeResponseDTO toResponse(Income entity) {
        IncomeResponseDTO response = new IncomeResponseDTO();
        response.setId(entity.getId());
        response.setDescription(entity.getDescription());
        response.setAmount(entity.getAmount());
        response.setOccurredAt(entity.getOccurredAt());
        response.setFarmId(entity.getFarmId());
        response.setUserId(entity.getUserId());
        response.setTransactionId(entity.getTransactionId());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
