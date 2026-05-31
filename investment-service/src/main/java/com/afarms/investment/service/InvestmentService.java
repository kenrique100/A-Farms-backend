package com.afarms.investment.service;

import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvestmentService {

    InvestmentResponseDTO create(String authHeader, InvestmentRequestDTO request);

    Page<InvestmentResponseDTO> findAll(
            String authHeader,
            UUID farmId,
            UUID userId,
            LocalDate start,
            LocalDate end,
            Pageable pageable);

    InvestmentResponseDTO findById(String authHeader, Long id);

    InvestmentResponseDTO update(String authHeader, Long id, InvestmentRequestDTO request);

    void delete(String authHeader, Long id);
}
