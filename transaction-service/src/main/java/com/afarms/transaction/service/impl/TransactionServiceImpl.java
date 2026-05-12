package com.afarms.transaction.service.impl;

import com.afarms.transaction.exception.ResourceNotFoundException;
import com.afarms.transaction.model.dto.TransactionRequestDTO;
import com.afarms.transaction.model.dto.TransactionResponseDTO;
import com.afarms.transaction.model.entity.Transaction;
import com.afarms.transaction.model.mapper.TransactionMapper;
import com.afarms.transaction.repository.TransactionRepository;
import com.afarms.transaction.service.TransactionService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TransactionResponseDTO create(TransactionRequestDTO request) {
        Transaction entity = TransactionMapper.toEntity(request);
        return TransactionMapper.toResponse(transactionRepository.save(entity));
    }

    @Override
    public List<TransactionResponseDTO> findAll() {
        return transactionRepository.findAll().stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

    @Override
    public TransactionResponseDTO findById(Long id) {
        return transactionRepository.findById(id)
                .map(TransactionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for id " + id));
    }
}
