package com.afarms.transaction.model.mapper;

import com.afarms.transaction.model.dto.InternalIncomeTransactionRequestDTO;
import com.afarms.transaction.model.dto.TransactionRequestDTO;
import com.afarms.transaction.model.dto.TransactionResponseDTO;
import com.afarms.transaction.model.entity.Transaction;
import java.util.UUID;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static Transaction toManualEntity(TransactionRequestDTO request, UUID farmId, UUID userId) {
        Transaction entity = new Transaction();
        entity.setDescription(request.getDescription());
        entity.setAmount(request.getAmount());
        entity.setOccurredAt(request.getOccurredAt());
        entity.setFarmId(farmId);
        entity.setUserId(userId);
        entity.setType("MANUAL");
        entity.setSourceService("TRANSACTION_SERVICE");
        entity.setSourceReferenceId(null);
        return entity;
    }

    public static Transaction toIncomeEntity(InternalIncomeTransactionRequestDTO request) {
        Transaction entity = new Transaction();
        entity.setDescription(request.getDescription());
        entity.setAmount(request.getAmount());
        entity.setOccurredAt(request.getOccurredAt());
        entity.setFarmId(request.getFarmId());
        entity.setUserId(request.getUserId());
        entity.setType("INCOME");
        entity.setSourceService("INCOME_SERVICE");
        entity.setSourceReferenceId(request.getIncomeId());
        return entity;
    }

    public static TransactionResponseDTO toResponse(Transaction entity) {
        TransactionResponseDTO response = new TransactionResponseDTO();
        response.setId(entity.getId());
        response.setDescription(entity.getDescription());
        response.setAmount(entity.getAmount());
        response.setOccurredAt(entity.getOccurredAt());
        response.setFarmId(entity.getFarmId());
        response.setUserId(entity.getUserId());
        response.setType(entity.getType());
        response.setSourceService(entity.getSourceService());
        response.setSourceReferenceId(entity.getSourceReferenceId());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
