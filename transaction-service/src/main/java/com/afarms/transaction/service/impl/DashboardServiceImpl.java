package com.afarms.transaction.service.impl;

import com.afarms.transaction.model.dto.TransactionCreateRequestDTO;
import com.afarms.transaction.model.dto.TransactionCreateResponseDTO;
import com.afarms.transaction.model.entity.Transaction;
import com.afarms.transaction.repository.TransactionRepository;
import com.afarms.transaction.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;

    @Override
    public TransactionCreateResponseDTO create(TransactionCreateRequestDTO request) {
        Transaction tx = Transaction.builder()
                .type(request.getType())
                .referenceId(request.getReferenceId())
                .transactionDate(request.getDate())
                .amount(request.getAmount())
                .createdBy(request.getCreatedBy())
                .farmId(request.getFarmId())
                .userId(request.getUserId())
                .build();

        Transaction saved = transactionRepository.save(tx);
        log.info("Created {} transaction with id {} for reference {}",
                request.getType(), saved.getId(), request.getReferenceId());

        return new TransactionCreateResponseDTO(saved.getId(), "Transaction created");
    }
}