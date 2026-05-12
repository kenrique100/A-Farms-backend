package com.afarms.investment.service;

import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import java.util.List;

public interface InvestmentService {

    InvestmentResponseDTO create(InvestmentRequestDTO request);

    List<InvestmentResponseDTO> findAll();

    InvestmentResponseDTO findById(Long id);
}
