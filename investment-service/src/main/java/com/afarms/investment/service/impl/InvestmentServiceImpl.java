package com.afarms.investment.service.impl;

import com.afarms.investment.client.TransactionServiceClient;
import com.afarms.investment.exception.BusinessException;
import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.model.dto.TokenValidationResponse;
import com.afarms.investment.model.dto.TransactionCreateRequestDTO;
import com.afarms.investment.model.dto.TransactionCreateResponseDTO;
import com.afarms.investment.model.entity.Investment;
import com.afarms.investment.model.mapper.InvestmentMapper;
import com.afarms.investment.repository.InvestmentRepository;
import com.afarms.investment.service.InvestmentService;
import com.afarms.investment.utils.InvestmentServiceUtils;
import com.afarms.investment.utils.InvestmentValidationUtils;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvestmentServiceImpl implements InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final TransactionServiceClient transactionServiceClient;
    private final InvestmentValidationUtils validationUtils;
    private final InvestmentServiceUtils serviceUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvestmentResponseDTO create(String authHeader, InvestmentRequestDTO request) {
        validationUtils.validateCreateRequest(request);
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        Investment entity = InvestmentMapper.toEntity(request);
        entity.setFarmId(tokenInfo.getFarmId());
        entity.setUserId(tokenInfo.getUserId());

        Investment saved = investmentRepository.save(entity);

        try {
            TransactionCreateRequestDTO txRequest = new TransactionCreateRequestDTO();
            txRequest.setType("INVESTMENT");
            txRequest.setReferenceId(saved.getId());
            txRequest.setDate(saved.getOccurredAt());
            txRequest.setAmount(saved.getAmount());
            txRequest.setCreatedBy(tokenInfo.getUsername());
            txRequest.setFarmId(tokenInfo.getFarmId());
            txRequest.setUserId(tokenInfo.getUserId());

            TransactionCreateResponseDTO tx = transactionServiceClient.createInvestmentTransaction(txRequest, authHeader);
            saved.setTransactionId(tx.getId());
            return InvestmentMapper.toResponse(investmentRepository.save(saved));
        } catch (Exception ex) {
            investmentRepository.delete(saved);
            throw new BusinessException("Unable to create linked transaction: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Page<InvestmentResponseDTO> findAll(
            String authHeader,
            UUID farmId,
            UUID userId,
            LocalDate start,
            LocalDate end,
            Pageable pageable) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);

        UUID scopedFarmId = serviceUtils.resolveScopeFarmId(farmId, tokenInfo);
        UUID scopedUserId = serviceUtils.resolveScopeUserId(userId, tokenInfo);
        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessException("Start date cannot be after end date");
        }

        Page<Investment> page;
        if (start != null && end != null && scopedUserId != null) {
            page = investmentRepository.findByFarmIdAndUserIdAndOccurredAtBetween(scopedFarmId, scopedUserId, start, end, pageable);
        } else if (start != null && end != null) {
            page = investmentRepository.findByFarmIdAndOccurredAtBetween(scopedFarmId, start, end, pageable);
        } else if (scopedUserId != null) {
            page = investmentRepository.findByFarmIdAndUserId(scopedFarmId, scopedUserId, pageable);
        } else {
            page = investmentRepository.findByFarmId(scopedFarmId, pageable);
        }

        return page.map(InvestmentMapper::toResponse);
    }

    @Override
    public InvestmentResponseDTO findById(String authHeader, Long id) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Investment investment = serviceUtils.findByIdAndFarmId(id, tokenInfo.getFarmId());
        return InvestmentMapper.toResponse(investment);
    }

    @Override
    @Transactional
    public InvestmentResponseDTO update(String authHeader, Long id, InvestmentRequestDTO request) {
        validationUtils.validateCreateRequest(request);
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        Investment investment = serviceUtils.findByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(investment, tokenInfo.getUserId(), tokenInfo.getRole());

        InvestmentMapper.updateEntity(investment, request);
        return InvestmentMapper.toResponse(investmentRepository.save(investment));
    }

    @Override
    @Transactional
    public void delete(String authHeader, Long id) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        Investment investment = serviceUtils.findByIdAndFarmId(id, tokenInfo.getFarmId());
        serviceUtils.checkOwnershipOrMaster(investment, tokenInfo.getUserId(), tokenInfo.getRole());
        investmentRepository.delete(investment);
    }
}
