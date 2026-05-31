package com.afarms.expense.service;

import com.afarms.expense.model.dto.ExpenseRequestDTO;
import com.afarms.expense.model.dto.ExpenseResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExpenseService {
    ExpenseResponseDTO create(String authHeader, ExpenseRequestDTO request);
    Page<ExpenseResponseDTO> findAll(String authHeader, Pageable pageable);
    List<ExpenseResponseDTO> findAllList(String authHeader);
    ExpenseResponseDTO findById(String authHeader, Long id);
    ExpenseResponseDTO update(String authHeader, Long id, ExpenseRequestDTO request);
    void delete(String authHeader, Long id);
    Page<ExpenseResponseDTO> findByFarmId(String authHeader, UUID farmId, Pageable pageable);
    Page<ExpenseResponseDTO> findByDateRange(String authHeader, LocalDate start, LocalDate end, Pageable pageable);
}