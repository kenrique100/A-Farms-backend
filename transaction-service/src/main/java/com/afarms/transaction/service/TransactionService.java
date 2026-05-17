package com.afarms.transaction.service;

import com.afarms.transaction.model.dto.TransactionCreateRequest;
import com.afarms.transaction.model.dto.TransactionCreateResponse;

public interface TransactionService {
    TransactionCreateResponse createIncomeTransaction(TransactionCreateRequest request, String apiKey);
}