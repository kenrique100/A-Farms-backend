package com.afarms.expense.service.impl;

import com.afarms.expense.exception.ResourceNotFoundException;
import com.afarms.expense.model.dto.ExpenseRequestDTO;
import com.afarms.expense.model.dto.ExpenseResponseDTO;
import com.afarms.expense.model.entity.Expense;
import com.afarms.expense.model.mapper.ExpenseMapper;
import com.afarms.expense.repository.ExpenseRepository;
import com.afarms.expense.service.ExpenseService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public ExpenseResponseDTO create(ExpenseRequestDTO request) {
        Expense entity = ExpenseMapper.toEntity(request);
        return ExpenseMapper.toResponse(expenseRepository.save(entity));
    }

    @Override
    public List<ExpenseResponseDTO> findAll() {
        return expenseRepository.findAll().stream()
                .map(ExpenseMapper::toResponse)
                .toList();
    }

    @Override
    public ExpenseResponseDTO findById(Long id) {
        return expenseRepository.findById(id)
                .map(ExpenseMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found for id " + id));
    }
}
