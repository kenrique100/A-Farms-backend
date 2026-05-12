package com.afarms.transaction.model.mapper;

import com.afarms.transaction.model.dto.TransactionRequestDTO;
import com.afarms.transaction.model.dto.TransactionResponseDTO;
import com.afarms.transaction.model.entity.Transaction;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static Transaction toEntity(TransactionRequestDTO request) {
        Transaction entity = new Transaction();
        entity.setDescription(request.getDescription());
        entity.setAmount(request.getAmount());
        entity.setOccurredAt(request.getOccurredAt());
        return entity;
    }

    public static TransactionResponseDTO toResponse(Transaction entity) {
        TransactionResponseDTO response = new TransactionResponseDTO();
        response.setId(entity.getId());
        response.setDescription(entity.getDescription());
        response.setAmount(entity.getAmount());
        response.setOccurredAt(entity.getOccurredAt());
        return response;
    }
}
