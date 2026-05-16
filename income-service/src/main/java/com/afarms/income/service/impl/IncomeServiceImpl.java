package com.afarms.income.service.impl;

import com.afarms.income.client.TransactionServiceClient;
import com.afarms.income.client.UserServiceClient;
import com.afarms.income.exception.ResourceNotFoundException;
import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.IncomeResponseDTO;
import com.afarms.income.model.dto.TokenValidationResponse;
import com.afarms.income.model.dto.TransactionCreateRequestDTO;
import com.afarms.income.model.dto.TransactionCreateResponseDTO;
import com.afarms.income.model.entity.Income;
import com.afarms.income.model.mapper.IncomeMapper;
import com.afarms.income.repository.IncomeRepository;
import com.afarms.income.service.IncomeService;
import com.afarms.income.utils.IncomeValidationUtils;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserServiceClient userServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    private final IncomeValidationUtils incomeValidationUtils;

    public IncomeServiceImpl(IncomeRepository incomeRepository,
                             UserServiceClient userServiceClient,
                             TransactionServiceClient transactionServiceClient,
                             IncomeValidationUtils incomeValidationUtils) {
        this.incomeRepository = incomeRepository;
        this.userServiceClient = userServiceClient;
        this.transactionServiceClient = transactionServiceClient;
        this.incomeValidationUtils = incomeValidationUtils;
    }

    @Override
    @Transactional
    public IncomeResponseDTO create(String authHeader, IncomeRequestDTO request) {
        incomeValidationUtils.validateAuthorizationHeader(authHeader);
        incomeValidationUtils.validateCreateIncomeRequest(request);

        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        incomeValidationUtils.validateTokenPayload(tokenInfo);

        Income entity = IncomeMapper.toEntity(request, tokenInfo.getFarmId(), tokenInfo.getUserId());
        Income savedIncome = incomeRepository.save(entity);

        TransactionCreateRequestDTO transactionRequest = new TransactionCreateRequestDTO();
        transactionRequest.setIncomeId(savedIncome.getId());
        transactionRequest.setFarmId(savedIncome.getFarmId());
        transactionRequest.setUserId(savedIncome.getUserId());
        transactionRequest.setDescription(savedIncome.getDescription());
        transactionRequest.setAmount(savedIncome.getAmount());
        transactionRequest.setOccurredAt(savedIncome.getOccurredAt());

        TransactionCreateResponseDTO transaction = transactionServiceClient.createIncomeTransaction(transactionRequest);
        savedIncome.setTransactionId(transaction.getId());

        return IncomeMapper.toResponse(incomeRepository.save(savedIncome));
    }

    @Override
    public List<IncomeResponseDTO> findAll(String authHeader) {
        incomeValidationUtils.validateAuthorizationHeader(authHeader);

        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        incomeValidationUtils.validateTokenPayload(tokenInfo);

        return incomeRepository.findByFarmId(tokenInfo.getFarmId()).stream()
                .map(IncomeMapper::toResponse)
                .toList();
    }

    @Override
    public IncomeResponseDTO findById(String authHeader, Long id) {
        incomeValidationUtils.validateAuthorizationHeader(authHeader);

        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        incomeValidationUtils.validateTokenPayload(tokenInfo);

        return incomeRepository.findByIdAndFarmId(id, tokenInfo.getFarmId())
                .map(IncomeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found for id " + id));
    }
}
