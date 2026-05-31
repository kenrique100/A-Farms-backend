package com.afarms.expense.service.impl;

import com.afarms.expense.client.TransactionServiceClient;
import com.afarms.expense.exception.BusinessException;
import com.afarms.expense.model.dto.ExpenseRequestDTO;
import com.afarms.expense.model.dto.ExpenseResponseDTO;
import com.afarms.expense.model.dto.TokenValidationResponse;
import com.afarms.expense.model.dto.TransactionCreateRequestDTO;
import com.afarms.expense.model.dto.TransactionCreateResponseDTO;
import com.afarms.expense.model.entity.Expense;
import com.afarms.expense.model.mapper.ExpenseMapper;
import com.afarms.expense.repository.ExpenseRepository;
import com.afarms.expense.service.ExpenseService;
import com.afarms.expense.utils.ExpenseServiceUtils;
import com.afarms.expense.utils.ExpenseValidationUtils;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TransactionServiceClient transactionServiceClient;
    private final ExpenseValidationUtils validationUtils;
    private final ExpenseServiceUtils serviceUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpenseResponseDTO create(String authHeader, ExpenseRequestDTO request) {
        validationUtils.validateCreateRequest(request);
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        Expense entity = ExpenseMapper.toEntity(request);
        entity.setFarmId(tokenInfo.getFarmId());
        entity.setUserId(tokenInfo.getUserId());

        Expense saved = expenseRepository.save(entity);

        try {
            TransactionCreateRequestDTO txRequest = new TransactionCreateRequestDTO();
            txRequest.setType("EXPENSE");
            txRequest.setReferenceId(saved.getId());
            txRequest.setDate(saved.getOccurredAt());
            txRequest.setAmount(saved.getAmount());
            txRequest.setCreatedBy(tokenInfo.getUsername());
            txRequest.setFarmId(tokenInfo.getFarmId());
            txRequest.setUserId(tokenInfo.getUserId());

            TransactionCreateResponseDTO tx = transactionServiceClient.createExpenseTransaction(txRequest, authHeader);
            saved.setTransactionId(tx.getId());
            return ExpenseMapper.toResponse(expenseRepository.save(saved));
        } catch (Exception ex) {
            throw new BusinessException("Unable to create linked transaction: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Page<ExpenseResponseDTO> findAll(
            String authHeader,
            UUID farmId,
            UUID userId,
            LocalDate start,
            LocalDate end,
            Pageable pageable) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);

        UUID scopedFarmId = serviceUtils.resolveScopeFarmId(farmId, tokenInfo);
        UUID scopedUserId = serviceUtils.resolveScopeUserId(userId, tokenInfo);
        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessException("Start date cannot be after end date");
        }

        Page<Expense> page;
        if (start != null && end != null && scopedUserId != null) {
            page = expenseRepository.findByFarmIdAndUserIdAndOccurredAtBetween(scopedFarmId, scopedUserId, start, end, pageable);
        } else if (start != null && end != null) {
            page = expenseRepository.findByFarmIdAndOccurredAtBetween(scopedFarmId, start, end, pageable);
        } else if (scopedUserId != null) {
            page = expenseRepository.findByFarmIdAndUserId(scopedFarmId, scopedUserId, pageable);
        } else {
            page = expenseRepository.findByFarmId(scopedFarmId, pageable);
        }

        return page.map(ExpenseMapper::toResponse);
    }

    @Override
    public ExpenseResponseDTO findById(String authHeader, Long id) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Expense expense = serviceUtils.findByIdAndFarmId(id, tokenInfo.getFarmId());
        return ExpenseMapper.toResponse(expense);
    }

    @Override
    @Transactional
    public ExpenseResponseDTO update(String authHeader, Long id, ExpenseRequestDTO request) {
        validationUtils.validateCreateRequest(request);
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        Expense expense = serviceUtils.findByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(expense, tokenInfo.getUserId(), tokenInfo.getRole());

        ExpenseMapper.updateEntity(expense, request);
        return ExpenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Override
    @Transactional
    public void delete(String authHeader, Long id) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Expense expense = serviceUtils.findByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(expense, tokenInfo.getUserId(), tokenInfo.getRole());
        expenseRepository.delete(expense);
    }
}
