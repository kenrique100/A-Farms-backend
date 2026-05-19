package com.afarms.income.utils;

import com.afarms.income.model.dto.IncomeResponseDTO;
import com.afarms.income.model.entity.Income;
import org.springframework.stereotype.Component;

@Component
public class ResponseBuilderUtils {

    public IncomeResponseDTO buildResponse(Income income, String userName) {
        return IncomeResponseDTO.builder()
                .id(income.getId())
                .description(income.getDescription())
                .amount(income.getAmount())
                .occurredAt(income.getOccurredAt())
                .farmId(income.getFarmId())
                .userId(income.getUserId())
                .userName(userName)
                .transactionId(income.getTransactionId())
                .createdAt(income.getCreatedAt())
                .build();
    }
}