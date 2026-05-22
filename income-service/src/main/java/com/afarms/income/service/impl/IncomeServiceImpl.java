package com.afarms.income.service.impl;

import com.afarms.income.client.TransactionServiceClient;
import com.afarms.income.exception.BusinessException;
import com.afarms.income.exception.ExternalServiceException;
import com.afarms.income.model.dto.*;
import com.afarms.income.model.entity.Income;
import com.afarms.income.repository.IncomeRepository;
import com.afarms.income.service.IncomeService;
import com.afarms.income.utils.IncomeBuilderUtils;
import com.afarms.income.utils.IncomeServiceUtils;
import com.afarms.income.utils.IncomeValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final TransactionServiceClient transactionServiceClient;
    private final IncomeBuilderUtils builderUtils;
    private final IncomeValidationUtils validationUtils;
    private final IncomeServiceUtils serviceUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IncomeResponseDTO create(String authHeader, IncomeRequestDTO request) {
        log.debug("Creating income for farm");
        validationUtils.validateCreateRequest(request);

        TokenValidationResponse tokenInfo =
                serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        Income income = builderUtils.buildIncomeFromRequest(request, tokenInfo);
        Income saved = incomeRepository.save(income);
        log.info("Income saved with id: {} for farm: {}", saved.getId(), tokenInfo.getFarmId());

        try {
            TransactionCreateRequestDTO incomeTxRequest = builderUtils.buildTransactionRequest(saved);

            // Forward JWT to transaction service
            TransactionCreateResponseDTO txResponse =
                    transactionServiceClient.createIncomeTransaction(incomeTxRequest, authHeader);

            saved.setTransactionId(txResponse.getId());
            Income updated = incomeRepository.save(saved);
            log.info("Transaction linked: {} for income {}", txResponse.getId(), updated.getId());

            return serviceUtils.toResponseDTOWithFetch(updated, tokenInfo);

        } catch (ExternalServiceException e) {
            log.error("Failed to create transaction for income {}: {}", saved.getId(), e.getMessage());
            incomeRepository.delete(saved);
            throw new BusinessException("Unable to create linked transaction: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating transaction for income {}: {}", saved.getId(), e.getMessage(), e);
            incomeRepository.delete(saved);
            throw new BusinessException("Unexpected error creating transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<IncomeResponseDTO> findAll(String authHeader, Pageable pageable) {
        log.debug("Entering findAll with authHeader present: {}", authHeader != null);
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        log.debug("Token validated, farmId: {}", tokenInfo.getFarmId());
        Page<Income> incomes = incomeRepository.findByFarmId(tokenInfo.getFarmId(), pageable);
        return serviceUtils.toResponseDTOPage(incomes, tokenInfo);
    }

    @Override
    public List<IncomeResponseDTO> findAllList(String authHeader) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        List<Income> incomes = incomeRepository.findByFarmId(tokenInfo.getFarmId());
        return serviceUtils.toResponseDTOList(incomes, tokenInfo);
    }

    @Override
    public IncomeResponseDTO findById(String authHeader, Long id) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Income income = serviceUtils.findIncomeByIdAndFarmId(id, tokenInfo.getFarmId());
        return serviceUtils.toResponseDTOWithFetch(income, tokenInfo);
    }

    @Override
    @Transactional
    public IncomeResponseDTO update(String authHeader, Long id, IncomeRequestDTO request) {
        validationUtils.validateCreateRequest(request);
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);
        Income existing = serviceUtils.findIncomeByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(existing, tokenInfo.getUserId(), tokenInfo.getRole());

        builderUtils.updateIncomeFromRequest(existing, request);
        Income updated = incomeRepository.save(existing);
        log.info("Income updated: {}", id);
        return serviceUtils.toResponseDTOWithFetch(updated, tokenInfo);
    }

    @Override
    @Transactional
    public void delete(String authHeader, Long id) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Income income = serviceUtils.findIncomeByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(income, tokenInfo.getUserId(), tokenInfo.getRole());
        incomeRepository.delete(income);
        log.info("Income deleted: {}", id);
    }

    @Override
    public Page<IncomeResponseDTO> findByFarmId(String authHeader, UUID farmId, Pageable pageable) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        serviceUtils.checkFarmAccess(farmId, tokenInfo.getFarmId(), tokenInfo.getRole());
        Page<Income> incomes = incomeRepository.findByFarmId(farmId, pageable);
        return serviceUtils.toResponseDTOPage(incomes, tokenInfo);
    }

    @Override
    public Page<IncomeResponseDTO> findByDateRange(String authHeader, LocalDate start, LocalDate end, Pageable pageable) {
        if (start.isAfter(end)) {
            throw new BusinessException("Start date cannot be after end date");
        }
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Page<Income> incomes =
                incomeRepository.findByFarmIdAndOccurredAtBetween(tokenInfo.getFarmId(), start, end, pageable);
        return serviceUtils.toResponseDTOPage(incomes, tokenInfo);
    }
}