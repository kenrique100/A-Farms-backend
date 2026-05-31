package com.afarms.expense.service.impl;

import com.afarms.expense.exception.BusinessException;
import com.afarms.expense.model.dto.ExpenseRequestDTO;
import com.afarms.expense.model.dto.ExpenseResponseDTO;
import com.afarms.expense.model.dto.TokenValidationResponse;
import com.afarms.expense.model.entity.Expense;
import com.afarms.expense.repository.ExpenseRepository;
import com.afarms.expense.service.ExpenseService;
import com.afarms.expense.utils.ExpenseBuilderUtils;
import com.afarms.expense.utils.ExpenseServiceUtils;
import com.afarms.expense.utils.ExpenseValidationUtils;
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
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseBuilderUtils builderUtils;
    private final ExpenseValidationUtils validationUtils;
    private final ExpenseServiceUtils serviceUtils;

    @Override
    @Transactional
    public ExpenseResponseDTO create(String authHeader, ExpenseRequestDTO request) {
        log.debug("Creating expense for farm");
        validationUtils.validateCreateRequest(request);

        TokenValidationResponse tokenInfo =
                serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        Expense expense = builderUtils.buildExpenseFromRequest(request, tokenInfo);
        Expense saved = expenseRepository.save(expense);
        log.info("Expense saved with id: {} for farm: {}", saved.getId(), tokenInfo.getFarmId());

        Map<UUID, String> singleUserMap = Map.of(tokenInfo.getUserId(), tokenInfo.getUsername());
        return serviceUtils.toResponseDTOWithFetch(saved, tokenInfo, singleUserMap);
    }

    @Override
    public Page<ExpenseResponseDTO> findAll(String authHeader, Pageable pageable) {
        log.debug("Entering findAll with authHeader present: {}", authHeader != null);
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        log.debug("Token validated, farmId: {}", tokenInfo.getFarmId());

        Page<Expense> expenses = expenseRepository.findByFarmId(tokenInfo.getFarmId(), pageable);
        Map<UUID, String> usernameMap = serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());

        return serviceUtils.toResponseDTOPage(expenses, tokenInfo, usernameMap);
    }

    @Override
    public List<ExpenseResponseDTO> findAllList(String authHeader) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);

        List<Expense> expenses = expenseRepository.findByFarmId(tokenInfo.getFarmId());
        Map<UUID, String> usernameMap = serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());

        return serviceUtils.toResponseDTOList(expenses, tokenInfo, usernameMap);
    }

    @Override
    public ExpenseResponseDTO findById(String authHeader, Long id) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);

        Expense expense = serviceUtils.findExpenseByIdAndFarmId(id, tokenInfo.getFarmId());
        Map<UUID, String> usernameMap = serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());

        return serviceUtils.toResponseDTOWithFetch(expense, tokenInfo, usernameMap);
    }

    @Override
    @Transactional
    public ExpenseResponseDTO update(String authHeader, Long id, ExpenseRequestDTO request) {
        validationUtils.validateCreateRequest(request);
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        Expense existing = serviceUtils.findExpenseByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(existing, tokenInfo.getUserId(), tokenInfo.getRole());

        builderUtils.updateExpenseFromRequest(existing, request);
        Expense updated = expenseRepository.save(existing);
        log.info("Expense updated: {}", id);

        Map<UUID, String> usernameMap = serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());

        return serviceUtils.toResponseDTOWithFetch(updated, tokenInfo, usernameMap);
    }

    @Override
    @Transactional
    public void delete(String authHeader, Long id) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);

        Expense expense = serviceUtils.findExpenseByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(expense, tokenInfo.getUserId(), tokenInfo.getRole());

        expenseRepository.delete(expense);
        log.info("Expense deleted: {}", id);
    }

    @Override
    public Page<ExpenseResponseDTO> findByFarmId(String authHeader, UUID farmId, Pageable pageable) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        serviceUtils.checkFarmAccess(farmId, tokenInfo.getFarmId(), tokenInfo.getRole());

        Page<Expense> expenses = expenseRepository.findByFarmId(farmId, pageable);
        Map<UUID, String> usernameMap = serviceUtils.getUsernameMapForFarm(authHeader, farmId);

        return serviceUtils.toResponseDTOPage(expenses, tokenInfo, usernameMap);
    }

    @Override
    public Page<ExpenseResponseDTO> findByDateRange(String authHeader, LocalDate start, LocalDate end, Pageable pageable) {
        if (start == null || end == null) {
            throw new BusinessException("Start date and end date are required parameters");
        }

        if (start.isAfter(end)) {
            throw new BusinessException("Start date cannot be after end date");
        }

        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);

        Page<Expense> expenses =
                expenseRepository.findByFarmIdAndOccurredAtBetween(tokenInfo.getFarmId(), start, end, pageable);
        Map<UUID, String> usernameMap = serviceUtils.getUsernameMapForFarm(authHeader, tokenInfo.getFarmId());

        return serviceUtils.toResponseDTOPage(expenses, tokenInfo, usernameMap);
    }
}