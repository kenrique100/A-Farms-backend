package com.afarms.transaction.service.impl;

import com.afarms.transaction.client.UserServiceClient;
import com.afarms.transaction.exception.BusinessException;
import com.afarms.transaction.exception.ResourceNotFoundException;
import com.afarms.transaction.model.dto.InternalIncomeTransactionRequestDTO;
import com.afarms.transaction.model.dto.TokenValidationResponse;
import com.afarms.transaction.model.dto.TransactionRequestDTO;
import com.afarms.transaction.model.dto.TransactionResponseDTO;
import com.afarms.transaction.model.entity.Transaction;
import com.afarms.transaction.model.mapper.TransactionMapper;
import com.afarms.transaction.repository.TransactionRepository;
import com.afarms.transaction.service.TransactionService;
import com.afarms.transaction.utils.TransactionValidationUtils;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserServiceClient userServiceClient;
    private final TransactionValidationUtils transactionValidationUtils;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  UserServiceClient userServiceClient,
                                  TransactionValidationUtils transactionValidationUtils) {
        this.transactionRepository = transactionRepository;
        this.userServiceClient = userServiceClient;
        this.transactionValidationUtils = transactionValidationUtils;
    }

    @Override
    @Transactional
    public TransactionResponseDTO create(String authHeader, TransactionRequestDTO request) {
        transactionValidationUtils.validateAuthorizationHeader(authHeader);
        transactionValidationUtils.validateManualCreateRequest(request);

        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        transactionValidationUtils.validateTokenPayload(tokenInfo);

        Transaction entity = TransactionMapper.toManualEntity(request, tokenInfo.getFarmId(), tokenInfo.getUserId());
        return TransactionMapper.toResponse(transactionRepository.save(entity));
    }

    @Override
    public List<TransactionResponseDTO> findAll(String authHeader) {
        transactionValidationUtils.validateAuthorizationHeader(authHeader);

        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        transactionValidationUtils.validateTokenPayload(tokenInfo);

        return transactionRepository.findByFarmId(tokenInfo.getFarmId()).stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

    @Override
    public TransactionResponseDTO findById(String authHeader, Long id) {
        transactionValidationUtils.validateAuthorizationHeader(authHeader);

        TokenValidationResponse tokenInfo = userServiceClient.validateToken(authHeader);
        transactionValidationUtils.validateTokenPayload(tokenInfo);

        return transactionRepository.findByIdAndFarmId(id, tokenInfo.getFarmId())
                .map(TransactionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for id " + id));
    }

    @Override
    @Transactional
    public TransactionResponseDTO createIncomeTransaction(String internalApiKey, InternalIncomeTransactionRequestDTO request) {
        transactionValidationUtils.validateInternalApiKey(internalApiKey);
        transactionValidationUtils.validateInternalIncomeRequest(request);

        if (transactionRepository.existsBySourceServiceAndSourceReferenceId("INCOME_SERVICE", request.getIncomeId())) {
            throw new BusinessException("Transaction for income " + request.getIncomeId() + " already exists");
        }

        Transaction entity = TransactionMapper.toIncomeEntity(request);
        return TransactionMapper.toResponse(transactionRepository.save(entity));
    }
}
