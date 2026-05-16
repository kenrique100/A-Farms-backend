package com.afarms.income.service;

import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.IncomeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IncomeService {
    IncomeResponseDTO create(String authHeader, IncomeRequestDTO request);
    Page<IncomeResponseDTO> findAll(String authHeader, Pageable pageable);
    List<IncomeResponseDTO> findAllList(String authHeader);
    IncomeResponseDTO findById(String authHeader, Long id);
    IncomeResponseDTO update(String authHeader, Long id, IncomeRequestDTO request);
    void delete(String authHeader, Long id);
    Page<IncomeResponseDTO> findByFarmId(String authHeader, UUID farmId, Pageable pageable);
    Page<IncomeResponseDTO> findByDateRange(String authHeader, LocalDate start, LocalDate end, Pageable pageable);
}