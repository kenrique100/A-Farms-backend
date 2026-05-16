package com.afarms.income.service;

import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.IncomeResponseDTO;
import java.util.List;

public interface IncomeService {

    IncomeResponseDTO create(String authHeader, IncomeRequestDTO request);

    List<IncomeResponseDTO> findAll(String authHeader);

    IncomeResponseDTO findById(String authHeader, Long id);
}
