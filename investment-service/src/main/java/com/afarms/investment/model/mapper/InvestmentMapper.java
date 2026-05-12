package com.afarms.investment.model.mapper;

import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.model.entity.Investment;

public final class InvestmentMapper {

    private InvestmentMapper() {
    }

    public static Investment toEntity(InvestmentRequestDTO request) {
        Investment entity = new Investment();
        entity.setDescription(request.getDescription());
        entity.setAmount(request.getAmount());
        entity.setOccurredAt(request.getOccurredAt());
        return entity;
    }

    public static InvestmentResponseDTO toResponse(Investment entity) {
        InvestmentResponseDTO response = new InvestmentResponseDTO();
        response.setId(entity.getId());
        response.setDescription(entity.getDescription());
        response.setAmount(entity.getAmount());
        response.setOccurredAt(entity.getOccurredAt());
        return response;
    }
}
