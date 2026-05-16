package com.afarms.income.service.impl;

import com.afarms.income.client.TransactionServiceClient;
import com.afarms.income.client.UserServiceClient;
import com.afarms.income.exception.AccessDeniedException;
import com.afarms.income.exception.BusinessException;
import com.afarms.income.exception.ResourceNotFoundException;
import com.afarms.income.model.dto.*;
import com.afarms.income.model.entity.Income;
import com.afarms.income.repository.IncomeRepository;
import com.afarms.income.service.IncomeService;
import com.afarms.income.utils.IncomeBuilderUtils;
import com.afarms.income.utils.IncomeValidationUtils;
import com.afarms.income.utils.ResponseBuilderUtils;
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
    private final UserServiceClient userServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    private final IncomeValidationUtils validationUtils;
    private final IncomeBuilderUtils builderUtils;
    private final ResponseBuilderUtils responseBuilder;

    @Override
    @Transactional
    public IncomeResponseDTO create(String authHeader, IncomeRequestDTO request) {
        log.debug("Create income request");

        validationUtils.validateAuthorizationHeader(authHeader);
        validationUtils.validateCreateRequest(request);

        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        validationUtils.validateTokenPayload(tokenInfo);
        validationUtils.validateRoleCanWrite(tokenInfo.getRole());

        Income income = builderUtils.buildIncomeFromRequest(request, tokenInfo);
        Income saved = incomeRepository.save(income);
        log.info("Income saved with id: {} for farm: {}", saved.getId(), tokenInfo.getFarmId());

        // Call transaction service
        TransactionCreateRequestDTO txRequest = builderUtils.buildTransactionRequest(saved);
        TransactionCreateResponseDTO txResponse = transactionServiceClient.createIncomeTransaction(txRequest);
        saved.setTransactionId(txResponse.getId());
        Income updated = incomeRepository.save(saved);
        log.info("Transaction linked: {} for income {}", txResponse.getId(), updated.getId());

        // Fetch user name
        UserDetailsResponse userDetails = userServiceClient.getUserById(tokenInfo.getUserId(), authHeader);
        return responseBuilder.buildResponse(updated, userDetails.getUsername());
    }

    @Override
    public Page<IncomeResponseDTO> findAll(String authHeader, Pageable pageable) {
        validationUtils.validateAuthorizationHeader(authHeader);
        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        validationUtils.validateTokenPayload(tokenInfo);

        Page<Income> incomes = incomeRepository.findByFarmId(tokenInfo.getFarmId(), pageable);
        return incomes.map(income -> {
            UserDetailsResponse user = userServiceClient.getUserById(income.getUserId(), authHeader);
            return responseBuilder.buildResponse(income, user.getUsername());
        });
    }

    @Override
    public List<IncomeResponseDTO> findAllList(String authHeader) {
        validationUtils.validateAuthorizationHeader(authHeader);
        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        validationUtils.validateTokenPayload(tokenInfo);

        List<Income> incomes = incomeRepository.findByFarmId(tokenInfo.getFarmId());
        return incomes.stream().map(income -> {
            UserDetailsResponse user = userServiceClient.getUserById(income.getUserId(), authHeader);
            return responseBuilder.buildResponse(income, user.getUsername());
        }).toList();
    }

    @Override
    public IncomeResponseDTO findById(String authHeader, Long id) {
        validationUtils.validateAuthorizationHeader(authHeader);
        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        validationUtils.validateTokenPayload(tokenInfo);

        Income income = incomeRepository.findByIdAndFarmId(id, tokenInfo.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Income not found with id: " + id));

        UserDetailsResponse user = userServiceClient.getUserById(income.getUserId(), authHeader);
        return responseBuilder.buildResponse(income, user.getUsername());
    }

    @Override
    @Transactional
    public IncomeResponseDTO update(String authHeader, Long id, IncomeRequestDTO request) {
        log.debug("Update income id: {}", id);
        validationUtils.validateAuthorizationHeader(authHeader);
        validationUtils.validateCreateRequest(request);

        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        validationUtils.validateTokenPayload(tokenInfo);
        validationUtils.validateRoleCanWrite(tokenInfo.getRole());

        Income existing = incomeRepository.findByIdAndFarmId(id, tokenInfo.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Income not found: " + id));

        // Only the creator or MASTER/ADMIN can update
        if (!existing.getUserId().equals(tokenInfo.getUserId()) && !"MASTER".equals(tokenInfo.getRole())) {
            throw new AccessDeniedException("You can only update your own incomes");
        }

        builderUtils.updateIncomeFromRequest(existing, request);
        Income updated = incomeRepository.save(existing);
        log.info("Income updated: {}", id);

        UserDetailsResponse user = userServiceClient.getUserById(updated.getUserId(), authHeader);
        return responseBuilder.buildResponse(updated, user.getUsername());
    }

    @Override
    @Transactional
    public void delete(String authHeader, Long id) {
        log.debug("Delete income id: {}", id);
        validationUtils.validateAuthorizationHeader(authHeader);
        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        validationUtils.validateTokenPayload(tokenInfo);

        Income income = incomeRepository.findByIdAndFarmId(id, tokenInfo.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Income not found: " + id));

        if (!income.getUserId().equals(tokenInfo.getUserId()) && !"MASTER".equals(tokenInfo.getRole())) {
            throw new AccessDeniedException("You can only delete your own incomes");
        }

        incomeRepository.delete(income);
        log.info("Income deleted: {}", id);
        // Optionally notify transaction service (async)
    }

    @Override
    public Page<IncomeResponseDTO> findByFarmId(String authHeader, UUID farmId, Pageable pageable) {
        validationUtils.validateAuthorizationHeader(authHeader);
        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        validationUtils.validateTokenPayload(tokenInfo);

        // Only MASTER or ADMIN can view other farms
        if (!tokenInfo.getFarmId().equals(farmId) && !"ADMIN".equals(tokenInfo.getRole())) {
            throw new AccessDeniedException("Cannot view incomes of another farm");
        }

        Page<Income> incomes = incomeRepository.findByFarmId(farmId, pageable);
        return incomes.map(income -> {
            UserDetailsResponse user = userServiceClient.getUserById(income.getUserId(), authHeader);
            return responseBuilder.buildResponse(income, user.getUsername());
        });
    }

    @Override
    public Page<IncomeResponseDTO> findByDateRange(String authHeader, LocalDate start, LocalDate end, Pageable pageable) {
        validationUtils.validateAuthorizationHeader(authHeader);
        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        validationUtils.validateTokenPayload(tokenInfo);

        if (start.isAfter(end)) {
            throw new BusinessException("Start date cannot be after end date");
        }

        Page<Income> incomes = incomeRepository.findByFarmIdAndOccurredAtBetween(tokenInfo.getFarmId(), start, end, pageable);
        return incomes.map(income -> {
            UserDetailsResponse user = userServiceClient.getUserById(income.getUserId(), authHeader);
            return responseBuilder.buildResponse(income, user.getUsername());
        });
    }
}