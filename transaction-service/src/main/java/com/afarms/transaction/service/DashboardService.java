package com.afarms.transaction.service;

import com.afarms.transaction.model.dto.TransactionCreateRequestDTO;
import com.afarms.transaction.model.dto.TransactionCreateResponseDTO;

public interface DashboardService {
    TransactionCreateResponseDTO create(TransactionCreateRequestDTO request);
}