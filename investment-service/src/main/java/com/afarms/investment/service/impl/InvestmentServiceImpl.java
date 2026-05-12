package com.afarms.investment.service.impl;

import com.afarms.investment.exception.ResourceNotFoundException;
import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.model.entity.Investment;
import com.afarms.investment.model.mapper.InvestmentMapper;
import com.afarms.investment.repository.InvestmentRepository;
import com.afarms.investment.service.InvestmentService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class InvestmentServiceImpl implements InvestmentService {

    private final InvestmentRepository investmentRepository;

    public InvestmentServiceImpl(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    @Override
    public InvestmentResponseDTO create(InvestmentRequestDTO request) {
        Investment entity = InvestmentMapper.toEntity(request);
        return InvestmentMapper.toResponse(investmentRepository.save(entity));
    }

    @Override
    public List<InvestmentResponseDTO> findAll() {
        return investmentRepository.findAll().stream()
                .map(InvestmentMapper::toResponse)
                .toList();
    }

    @Override
    public InvestmentResponseDTO findById(Long id) {
        return investmentRepository.findById(id)
                .map(InvestmentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found for id " + id));
    }
}
