package com.afarms.transaction.service.impl;

import com.afarms.transaction.client.UserServiceClient;
import com.afarms.transaction.exception.ExternalServiceException;
import com.afarms.transaction.model.dto.TokenValidationResponse;
import com.afarms.transaction.model.dto.TransactionCreateRequest;
import com.afarms.transaction.model.dto.TransactionCreateResponse;
import com.afarms.transaction.model.entity.Transaction;
import com.afarms.transaction.model.mapper.TransactionMapper;
import com.afarms.transaction.repository.TransactionRepository;
import com.afarms.transaction.service.TransactionService;
import com.afarms.transaction.utils.TransactionValidationUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserServiceClient userServiceClient;
    private final TransactionValidationUtils validationUtils;

    @Override
    @Transactional
    @Retry(name = "transactionRetry", fallbackMethod = "createIncomeTransactionFallback")
    @CircuitBreaker(name = "transactionService", fallbackMethod = "createIncomeTransactionFallback")
    public TransactionCreateResponse createIncomeTransaction(TransactionCreateRequest request, String apiKey) {
        // 1. Validate API key
        validationUtils.validateInternalApiKey(apiKey);

        // 2. Validate request data
        validationUtils.validateCreateRequest(request);

        // 3. Optionally validate user token if provided
        if (request.getAuthToken() != null && !request.getAuthToken().isEmpty()) {
            TokenValidationResponse validation = validateUserToken(request.getAuthToken());
            validationUtils.validateUserToken(validation);
        }

        // 4. Convert to entity (mapper)
        Transaction transaction = TransactionMapper.toIncomeEntity(request);

        // 5. Save
        Transaction saved = transactionRepository.save(transaction);
        log.info("Income transaction created with id {} for farm {}", saved.getId(), saved.getFarmId());

        // 6. Return response
        return TransactionMapper.toResponse(saved);
    }

    // Separate method for token validation
    @Retry(name = "userServiceRetry", fallbackMethod = "validateUserTokenFallback")
    @CircuitBreaker(name = "userServiceBreaker", fallbackMethod = "validateUserTokenFallback")
    private TokenValidationResponse validateUserToken(String token) {
        return userServiceClient.validateToken(token);
    }

    private TokenValidationResponse validateUserTokenFallback(String token, Exception ex) {
        log.error("User service unavailable: {}", ex.getMessage());
        throw new ExternalServiceException("Unable to validate user credentials. Please try again.");
    }

    // Fallback for circuit breaker / retry exhaustion
    public TransactionCreateResponse createIncomeTransactionFallback(TransactionCreateRequest request, String apiKey, Exception ex) {
        log.error("Circuit breaker open or retries exhausted for transaction creation: {}", ex.getMessage());
        throw new ExternalServiceException("Transaction service temporarily unavailable. Please try later.");
    }
}