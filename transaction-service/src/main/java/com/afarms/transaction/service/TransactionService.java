package com.afarms.transaction.service;

import com.afarms.transaction.model.dto.TransactionRequestDTO;
import com.afarms.transaction.model.dto.TransactionResponseDTO;
import java.util.List;

public interface TransactionService {

    TransactionResponseDTO create(TransactionRequestDTO request);

    List<TransactionResponseDTO> findAll();

    TransactionResponseDTO findById(Long id);
}
