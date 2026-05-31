package com.afarms.expense.service;

import com.afarms.expense.model.dto.ExpenseRequestDTO;
import com.afarms.expense.model.dto.ExpenseResponseDTO;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpenseService {

    ExpenseResponseDTO create(String authHeader, ExpenseRequestDTO request);

    Page<ExpenseResponseDTO> findAll(
            String authHeader,
            UUID farmId,
            UUID userId,
            LocalDate start,
            LocalDate end,
            Pageable pageable);

    ExpenseResponseDTO findById(String authHeader, Long id);

    ExpenseResponseDTO update(String authHeader, Long id, ExpenseRequestDTO request);

    void delete(String authHeader, Long id);
}
