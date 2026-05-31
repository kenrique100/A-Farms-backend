package com.afarms.income.service.impl;

import com.afarms.income.exception.BusinessException;
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
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeBuilderUtils builderUtils;
    private final IncomeValidationUtils validationUtils;
    private final IncomeServiceUtils serviceUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IncomeResponseDTO create(String authHeader, IncomeRequestDTO request) {
        validationUtils.validateCreateRequest(request);
        TokenValidationResponse tokenInfo =
                serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        Income income = builderUtils.buildIncomeFromRequest(request, tokenInfo);
        Income saved = incomeRepository.save(income);
        log.info("Income saved: id={} farm={}", saved.getId(), tokenInfo.getFarmId());

        Map<UUID, String> userMap = Map.of(tokenInfo.getUserId(), tokenInfo.getUsername());
        return serviceUtils.toResponseDTOWithFetch(authHeader, saved, tokenInfo, userMap);
    }

    @Override
    public Page<IncomeResponseDTO> findAll(String authHeader, Pageable pageable) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Page<Income> incomes = incomeRepository.findByFarmId(tokenInfo.getFarmId(), pageable);
        Map<UUID, String> usernameMap =
                serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());
        return serviceUtils.toResponseDTOPage(authHeader, incomes, tokenInfo, usernameMap);
    }

    @Override
    public List<IncomeResponseDTO> findAllList(String authHeader) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        List<Income> incomes = incomeRepository.findByFarmId(tokenInfo.getFarmId());
        Map<UUID, String> usernameMap =
                serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());
        return serviceUtils.toResponseDTOList(authHeader, incomes, tokenInfo, usernameMap);
    }

    @Override
    public IncomeResponseDTO findById(String authHeader, Long id) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Income income = serviceUtils.findIncomeByIdAndFarmId(id, tokenInfo.getFarmId());
        Map<UUID, String> usernameMap =
                serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());
        return serviceUtils.toResponseDTOWithFetch(authHeader, income, tokenInfo, usernameMap);
    }

    @Override
    @Transactional
    public IncomeResponseDTO update(String authHeader, Long id, IncomeRequestDTO request) {
        validationUtils.validateCreateRequest(request);
        TokenValidationResponse tokenInfo =
                serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        Income existing = serviceUtils.findIncomeByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(existing, tokenInfo.getUserId(), tokenInfo.getRole());

        builderUtils.updateIncomeFromRequest(existing, request);
        Income updated = incomeRepository.save(existing);
        log.info("Income updated: id={}", id);

        Map<UUID, String> usernameMap =
                serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());
        return serviceUtils.toResponseDTOWithFetch(authHeader, updated, tokenInfo, usernameMap);
    }

    @Override
    @Transactional
    public void delete(String authHeader, Long id) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Income income = serviceUtils.findIncomeByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(income, tokenInfo.getUserId(), tokenInfo.getRole());
        incomeRepository.delete(income);
        log.info("Income deleted: id={}", id);
    }

    @Override
    public Page<IncomeResponseDTO> findByFarmId(String authHeader, UUID farmId, Pageable pageable) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        serviceUtils.checkFarmAccess(farmId, tokenInfo.getFarmId(), tokenInfo.getRole());
        Page<Income> incomes = incomeRepository.findByFarmId(farmId, pageable);
        Map<UUID, String> usernameMap = serviceUtils.getUsernameMapForFarm(authHeader, farmId);
        return serviceUtils.toResponseDTOPage(authHeader, incomes, tokenInfo, usernameMap);
    }

    @Override
    public Page<IncomeResponseDTO> findByDateRange(String authHeader, LocalDate start,
                                                   LocalDate end, Pageable pageable) {
        if (start == null || end == null) {
            throw new BusinessException("Start date and end date are required");
        }
        if (start.isAfter(end)) {
            throw new BusinessException("Start date cannot be after end date");
        }
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Page<Income> incomes = incomeRepository.findByFarmIdAndOccurredAtBetween(
                tokenInfo.getFarmId(), start, end, pageable);
        Map<UUID, String> usernameMap =
                serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());
        return serviceUtils.toResponseDTOPage(authHeader, incomes, tokenInfo, usernameMap);
    }
}