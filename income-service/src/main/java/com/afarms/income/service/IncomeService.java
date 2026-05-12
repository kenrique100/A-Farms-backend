package com.afarms.income.service;

import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.IncomeResponseDTO;
import java.util.List;

public interface IncomeService {

    IncomeResponseDTO create(IncomeRequestDTO request);

    List<IncomeResponseDTO> findAll();

    IncomeResponseDTO findById(Long id);
}
