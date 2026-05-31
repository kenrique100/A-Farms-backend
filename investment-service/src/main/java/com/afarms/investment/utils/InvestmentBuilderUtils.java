package com.afarms.investment.utils;

import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.TokenValidationResponse;
import com.afarms.investment.model.entity.Investment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Slf4j
public class InvestmentBuilderUtils {

    public Investment buildFromRequest(InvestmentRequestDTO request, TokenValidationResponse tokenInfo) {
        log.debug("Building investment entity for farm: {}", tokenInfo.getFarmId());
        return Investment.builder()
                .initialAmount(request.getInitialAmount())
                .currentBalance(request.getCurrentBalance())
                .farmId(tokenInfo.getFarmId())
                .userId(tokenInfo.getUserId())
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
    }

    public void updateBalance(Investment investment, BigDecimal newBalance) {
        log.debug("Updating investment id={} balance from {} to {}",
                investment.getId(), investment.getCurrentBalance(), newBalance);
        investment.setCurrentBalance(newBalance);
        investment.setUpdatedAt(LocalDate.now());
    }

    public void updateFromRequest(Investment investment, InvestmentRequestDTO request) {
        log.debug("Updating investment id={} from request", investment.getId());
        investment.setInitialAmount(request.getInitialAmount());
        investment.setCurrentBalance(request.getCurrentBalance());
        investment.setUpdatedAt(LocalDate.now());
    }
}