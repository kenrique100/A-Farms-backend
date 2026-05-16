package com.afarms.transaction.service;

import com.afarms.transaction.model.dto.InternalIncomeTransactionRequestDTO;
import com.afarms.transaction.model.dto.TransactionRequestDTO;
import com.afarms.transaction.model.dto.TransactionResponseDTO;
import java.util.List;

public interface TransactionService {

    TransactionResponseDTO create(String authHeader, TransactionRequestDTO request);

    List<TransactionResponseDTO> findAll(String authHeader);

    TransactionResponseDTO findById(String authHeader, Long id);

    TransactionResponseDTO createIncomeTransaction(String internalApiKey, InternalIncomeTransactionRequestDTO request);
}
