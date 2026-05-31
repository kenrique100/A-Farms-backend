package com.afarms.investment.utils;

import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.model.entity.Investment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Slf4j
public class ResponseBuilderUtils {

    public InvestmentResponseDTO buildResponse(Investment investment, String username) {
        BigDecimal roi = computeRoi(investment.getInitialAmount(), investment.getCurrentBalance());
        log.debug("Building response for investment id={} roi={}%", investment.getId(), roi);
        return InvestmentResponseDTO.builder()
                .id(investment.getId())
                .initialAmount(investment.getInitialAmount())
                .currentBalance(investment.getCurrentBalance())
                .roi(roi)
                .farmId(investment.getFarmId())
                .userId(investment.getUserId())
                .userName(username)
                .createdAt(investment.getCreatedAt())
                .updatedAt(investment.getUpdatedAt())
                .createdTimestamp(investment.getCreatedTimestamp())
                .build();
    }

    private BigDecimal computeRoi(BigDecimal initialAmount, BigDecimal currentBalance) {
        if (initialAmount == null || initialAmount.compareTo(BigDecimal.ZERO) == 0
                || currentBalance == null) {
            return BigDecimal.ZERO;
        }
        // ROI = (currentBalance - initialAmount) / initialAmount * 100
        return currentBalance.subtract(initialAmount)
                .divide(initialAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}