package com.afarms.transaction.model.mapper;

import com.afarms.transaction.model.dto.TransactionCreateRequest;
import com.afarms.transaction.model.dto.TransactionCreateResponse;
import com.afarms.transaction.model.entity.Transaction;
import com.afarms.transaction.model.enums.TransactionStatus;
import com.afarms.transaction.model.enums.TransactionType;

public final class TransactionMapper {

    private TransactionMapper() {}

    public static Transaction toIncomeEntity(TransactionCreateRequest request) {
        TransactionType type = TransactionType.INCOME;
        if (request.getType() != null && !request.getType().isBlank()) {
            try {
                type = TransactionType.valueOf(request.getType().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        return Transaction.builder()
                .incomeId(request.getIncomeId())
                // .expenseId(request.getExpenseId())
                // .investmentId(request.getInvestmentId())
                .farmId(request.getFarmId())
                .userId(request.getUserId())
                .description(request.getDescription())
                .amount(request.getAmount())
                .occurredAt(request.getOccurredAt())
                .type(type)
                .status(TransactionStatus.COMPLETED)
                .build();
    }

    public static TransactionCreateResponse toResponse(Transaction entity) {
        return TransactionCreateResponse.builder()
                .id(entity.getId())
                .status(entity.getStatus().name())
                .message("Transaction recorded successfully")
                .build();
    }
}
