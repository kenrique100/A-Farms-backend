package com.afarms.income.utils;

import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.TokenValidationResponse;
import com.afarms.income.model.entity.Income;
import org.springframework.stereotype.Component;

@Component
public class IncomeBuilderUtils {

    public Income buildIncomeFromRequest(IncomeRequestDTO request, TokenValidationResponse tokenInfo) {
        return Income.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .occurredAt(request.getOccurredAt())
                .farmId(tokenInfo.getFarmId())
                .userId(tokenInfo.getUserId())
                .build();
    }

    public void updateIncomeFromRequest(Income income, IncomeRequestDTO request) {
        income.setDescription(request.getDescription());
        income.setAmount(request.getAmount());
        income.setOccurredAt(request.getOccurredAt());
    }
}