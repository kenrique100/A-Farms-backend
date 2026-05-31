package com.afarms.transaction.service;

import com.afarms.transaction.model.dto.TransactionCreateRequestDTO;
import com.afarms.transaction.model.dto.TransactionCreateResponseDTO;
import com.afarms.transaction.model.dto.TransactionResponseDTO;
import com.afarms.transaction.model.dto.TransactionSummaryDTO;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DashboardService {

    TransactionCreateResponseDTO create(String authHeader, TransactionCreateRequestDTO request, String type);

    Page<TransactionResponseDTO> list(
            String authHeader,
            UUID farmId,
            UUID userId,
            String type,
            LocalDate start,
            LocalDate end,
            Pageable pageable);

    TransactionSummaryDTO summary(String authHeader, UUID farmId, LocalDate start, LocalDate end);
}
