package com.afarms.investment.model.mapper;

import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.model.entity.Investment;

public final class InvestmentMapper {

    private InvestmentMapper() {
    }

    public static Investment toEntity(InvestmentRequestDTO request) {
        Investment entity = new Investment();
        updateEntity(entity, request);
        return entity;
    }

    public static void updateEntity(Investment entity, InvestmentRequestDTO request) {
        entity.setDescription(request.getDescription());
        entity.setAmount(request.getAmount());
        entity.setOccurredAt(request.getOccurredAt());
    }

    public static InvestmentResponseDTO toResponse(Investment entity) {
        InvestmentResponseDTO response = new InvestmentResponseDTO();
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
