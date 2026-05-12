package com.afarms.income.service.impl;

import com.afarms.income.exception.ResourceNotFoundException;
import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.IncomeResponseDTO;
import com.afarms.income.model.entity.Income;
import com.afarms.income.model.mapper.IncomeMapper;
import com.afarms.income.repository.IncomeRepository;
import com.afarms.income.service.IncomeService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;

    public IncomeServiceImpl(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    @Override
    public IncomeResponseDTO create(IncomeRequestDTO request) {
        Income entity = IncomeMapper.toEntity(request);
        return IncomeMapper.toResponse(incomeRepository.save(entity));
    }

    @Override
    public List<IncomeResponseDTO> findAll() {
        return incomeRepository.findAll().stream()
                .map(IncomeMapper::toResponse)
                .toList();
    }

    @Override
    public IncomeResponseDTO findById(Long id) {
        return incomeRepository.findById(id)
                .map(IncomeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found for id " + id));
    }
}
