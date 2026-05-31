package com.afarms.investment.service;

import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InvestmentService {
    InvestmentResponseDTO create(String authHeader, InvestmentRequestDTO request);
    Page<InvestmentResponseDTO> findAll(String authHeader, Pageable pageable);
    List<InvestmentResponseDTO> findAllList(String authHeader);
    InvestmentResponseDTO findById(String authHeader, Long id);
    InvestmentResponseDTO update(String authHeader, Long id, InvestmentRequestDTO request);
    InvestmentResponseDTO updateBalance(String authHeader, Long id, BigDecimal newBalance);
    void delete(String authHeader, Long id);
    Page<InvestmentResponseDTO> findByFarmId(String authHeader, UUID farmId, Pageable pageable);
    Page<InvestmentResponseDTO> findByDateRange(String authHeader, LocalDate start,
                                                LocalDate end, Pageable pageable);
    BigDecimal getTotalPortfolioBalance(String authHeader);
}