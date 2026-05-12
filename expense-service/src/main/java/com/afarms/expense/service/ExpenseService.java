package com.afarms.expense.service;

import com.afarms.expense.model.dto.ExpenseRequestDTO;
import com.afarms.expense.model.dto.ExpenseResponseDTO;
import java.util.List;

public interface ExpenseService {

    ExpenseResponseDTO create(ExpenseRequestDTO request);

    List<ExpenseResponseDTO> findAll();

    ExpenseResponseDTO findById(Long id);
}
