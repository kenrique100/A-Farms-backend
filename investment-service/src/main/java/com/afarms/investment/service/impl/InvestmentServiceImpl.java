package com.afarms.investment.service.impl;

import com.afarms.investment.exception.BusinessException;
import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.model.dto.TokenValidationResponse;
import com.afarms.investment.model.entity.Investment;
import com.afarms.investment.repository.InvestmentRepository;
import com.afarms.investment.service.InvestmentService;
import com.afarms.investment.utils.InvestmentBuilderUtils;
import com.afarms.investment.utils.InvestmentServiceUtils;
import com.afarms.investment.utils.InvestmentValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentServiceImpl implements InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final InvestmentBuilderUtils builderUtils;
    private final InvestmentValidationUtils validationUtils;
    private final InvestmentServiceUtils serviceUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvestmentResponseDTO create(String authHeader, InvestmentRequestDTO request) {
        log.info("Creating investment");
        validationUtils.validateCreateRequest(request);

        TokenValidationResponse tokenInfo =
                serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        Investment investment = builderUtils.buildFromRequest(request, tokenInfo);
        Investment saved = investmentRepository.save(investment);
        log.info("Investment created: id={} farm={} initialAmount={}",
                saved.getId(), tokenInfo.getFarmId(), saved.getInitialAmount());

        Map<UUID, String> userMap = Map.of(tokenInfo.getUserId(), tokenInfo.getUsername());
        return serviceUtils.toResponseDTO(authHeader, saved, tokenInfo, userMap);
    }

    @Override
    public Page<InvestmentResponseDTO> findAll(String authHeader, Pageable pageable) {
        log.debug("Finding all investments paginated");
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Page<Investment> investments = investmentRepository.findByFarmId(
                tokenInfo.getFarmId(), pageable);
        Map<UUID, String> usernameMap =
                serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());
        log.debug("Found {} investments for farm={}", investments.getTotalElements(), tokenInfo.getFarmId());
        return serviceUtils.toResponseDTOPage(authHeader, investments, tokenInfo, usernameMap);
    }

    @Override
    public List<InvestmentResponseDTO> findAllList(String authHeader) {
        log.debug("Finding all investments as list");
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        List<Investment> investments = investmentRepository.findByFarmId(tokenInfo.getFarmId());
        Map<UUID, String> usernameMap =
                serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());
        log.debug("Found {} investments for farm={}", investments.size(), tokenInfo.getFarmId());
        return serviceUtils.toResponseDTOList(authHeader, investments, tokenInfo, usernameMap);
    }

    @Override
    public InvestmentResponseDTO findById(String authHeader, Long id) {
        log.debug("Finding investment id={}", id);
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Investment investment = serviceUtils.findByIdAndFarmId(id, tokenInfo.getFarmId());
        Map<UUID, String> usernameMap =
                serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());
        return serviceUtils.toResponseDTO(authHeader, investment, tokenInfo, usernameMap);
    }

    @Override
    @Transactional
    public InvestmentResponseDTO update(String authHeader, Long id, InvestmentRequestDTO request) {
        log.info("Updating investment id={}", id);
        validationUtils.validateCreateRequest(request);
        TokenValidationResponse tokenInfo =
                serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        Investment existing = serviceUtils.findByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(existing, tokenInfo.getUserId(), tokenInfo.getRole());

        builderUtils.updateFromRequest(existing, request);
        Investment updated = investmentRepository.save(existing);
        log.info("Investment updated: id={} newBalance={}", id, updated.getCurrentBalance());

        Map<UUID, String> usernameMap =
                serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());
        return serviceUtils.toResponseDTO(authHeader, updated, tokenInfo, usernameMap);
    }

    @Override
    @Transactional
    public InvestmentResponseDTO updateBalance(String authHeader, Long id, BigDecimal newBalance) {
        log.info("Updating balance for investment id={} to {}", id, newBalance);
        validationUtils.validateUpdateBalance(newBalance);
        TokenValidationResponse tokenInfo =
                serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        Investment existing = serviceUtils.findByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(existing, tokenInfo.getUserId(), tokenInfo.getRole());

        builderUtils.updateBalance(existing, newBalance);
        Investment updated = investmentRepository.save(existing);
        log.info("Balance updated for investment id={}: {} → {}",
                id, existing.getCurrentBalance(), newBalance);

        Map<UUID, String> usernameMap =
                serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());
        return serviceUtils.toResponseDTO(authHeader, updated, tokenInfo, usernameMap);
    }

    @Override
    @Transactional
    public void delete(String authHeader, Long id) {
        log.info("Deleting investment id={}", id);
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Investment investment = serviceUtils.findByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(investment, tokenInfo.getUserId(), tokenInfo.getRole());
        investmentRepository.delete(investment);
        log.info("Investment deleted: id={}", id);
    }

    @Override
    public Page<InvestmentResponseDTO> findByFarmId(String authHeader, UUID farmId, Pageable pageable) {
        log.debug("Finding investments for farm={}", farmId);
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        serviceUtils.checkFarmAccess(farmId, tokenInfo.getFarmId(), tokenInfo.getRole());
        Page<Investment> investments = investmentRepository.findByFarmId(farmId, pageable);
        Map<UUID, String> usernameMap = serviceUtils.getUsernameMapForFarm(authHeader, farmId);
        return serviceUtils.toResponseDTOPage(authHeader, investments, tokenInfo, usernameMap);
    }

    @Override
    public Page<InvestmentResponseDTO> findByDateRange(String authHeader, LocalDate start,
                                                       LocalDate end, Pageable pageable) {
        log.debug("Finding investments by date range {}-{}", start, end);
        if (start == null || end == null) {
            throw new BusinessException("Start date and end date are required");
        }
        if (start.isAfter(end)) {
            throw new BusinessException("Start date cannot be after end date");
        }
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Page<Investment> investments = investmentRepository.findByFarmIdAndCreatedAtBetween(
                tokenInfo.getFarmId(), start, end, pageable);
        Map<UUID, String> usernameMap =
                serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());
        log.debug("Found {} investments in range for farm={}",
                investments.getTotalElements(), tokenInfo.getFarmId());
        return serviceUtils.toResponseDTOPage(authHeader, investments, tokenInfo, usernameMap);
    }

    @Override
    public BigDecimal getTotalPortfolioBalance(String authHeader) {
        log.debug("Calculating total portfolio balance");
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        BigDecimal total = investmentRepository.sumCurrentBalanceByFarmId(tokenInfo.getFarmId());
        BigDecimal result = total != null ? total : BigDecimal.ZERO;
        log.info("Total portfolio balance for farm={}: {}", tokenInfo.getFarmId(), result);
        return result;
    }
}