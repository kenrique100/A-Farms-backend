package com.afarms.transaction.service.impl;

import com.afarms.transaction.exception.BusinessException;
import com.afarms.transaction.model.dto.TokenValidationResponse;
import com.afarms.transaction.model.dto.TransactionCreateRequestDTO;
import com.afarms.transaction.model.dto.TransactionCreateResponseDTO;
import com.afarms.transaction.model.dto.TransactionResponseDTO;
import com.afarms.transaction.model.dto.TransactionSummaryDTO;
import com.afarms.transaction.model.entity.Transaction;
import com.afarms.transaction.repository.TransactionRepository;
import com.afarms.transaction.service.DashboardService;
import com.afarms.transaction.utils.TransactionServiceUtils;
import com.afarms.transaction.utils.TransactionValidationUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;
    private final TransactionValidationUtils validationUtils;
    private final TransactionServiceUtils serviceUtils;

    @Override
    public TransactionCreateResponseDTO create(String authHeader, TransactionCreateRequestDTO request, String type) {
        validationUtils.validateCreateRequest(request);
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader);

        UUID scopedFarm = serviceUtils.resolveScopeFarmId(request.getFarmId(), tokenInfo);
        String normalizedType = type.toUpperCase(Locale.ROOT);

        Optional<Transaction> existing = serviceUtils.findIdempotent(normalizedType, request.getReferenceId(), scopedFarm);
        if (existing.isPresent()) {
            return new TransactionCreateResponseDTO(existing.get().getId(), "Transaction already exists");
        }

        Transaction tx = Transaction.builder()
                .type(normalizedType)
                .referenceId(request.getReferenceId())
                .transactionDate(request.getDate())
                .amount(request.getAmount())
                .createdBy(request.getCreatedBy() == null ? tokenInfo.getUsername() : request.getCreatedBy())
                .farmId(scopedFarm)
                .userId(request.getUserId() == null ? tokenInfo.getUserId() : request.getUserId())
                .idempotencyKey(normalizedType + "-" + request.getReferenceId() + "-" + scopedFarm)
                .build();

        Transaction saved = transactionRepository.save(tx);
        return new TransactionCreateResponseDTO(saved.getId(), "Transaction created");
    }

    @Override
    public Page<TransactionResponseDTO> list(
            String authHeader,
            UUID farmId,
            UUID userId,
            String type,
            LocalDate start,
            LocalDate end,
            Pageable pageable) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        UUID scopedFarmId = serviceUtils.resolveScopeFarmId(farmId, tokenInfo);

        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessException("Start date cannot be after end date");
        }

        Specification<Transaction> spec = Specification.where(byFarm(scopedFarmId));
        if (userId != null) {
            if (!userId.equals(tokenInfo.getUserId())
                    && !"MASTER".equalsIgnoreCase(tokenInfo.getRole())
                    && !"ADMIN".equalsIgnoreCase(tokenInfo.getRole())) {
                throw new BusinessException("Cannot access transactions from another user");
            }
            spec = spec.and(byUser(userId));
        }
        if (type != null && !type.isBlank()) {
            spec = spec.and(byType(type.toUpperCase(Locale.ROOT)));
        }
        if (start != null) {
            spec = spec.and(onOrAfter(start));
        }
        if (end != null) {
            spec = spec.and(onOrBefore(end));
        }

        return transactionRepository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    @Override
    public TransactionSummaryDTO summary(String authHeader, UUID farmId, LocalDate start, LocalDate end) {
        TokenValidationResponse tokenInfo = serviceUtils.validateAndGetTokenInfo(authHeader);
        UUID scopedFarmId = serviceUtils.resolveScopeFarmId(farmId, tokenInfo);

        BigDecimal total = transactionRepository.sumAmountByFarmAndRange(scopedFarmId, start, end);
        BigDecimal income = transactionRepository.sumAmountByFarmTypeAndRange(scopedFarmId, "INCOME", start, end);
        BigDecimal expense = transactionRepository.sumAmountByFarmTypeAndRange(scopedFarmId, "EXPENSE", start, end);
        BigDecimal investment = transactionRepository.sumAmountByFarmTypeAndRange(scopedFarmId, "INVESTMENT", start, end);
        long count = transactionRepository.countByFarmAndRange(scopedFarmId, start, end);

        return new TransactionSummaryDTO(count, total, income, expense, investment);
    }

    private Specification<Transaction> byFarm(UUID farmId) {
        return (root, query, cb) -> cb.equal(root.get("farmId"), farmId);
    }

    private Specification<Transaction> byUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    private Specification<Transaction> byType(String type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    private Specification<Transaction> onOrAfter(LocalDate date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), date);
    }

    private Specification<Transaction> onOrBefore(LocalDate date) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("transactionDate"), date);
    }

    private TransactionResponseDTO toResponse(Transaction tx) {
        return new TransactionResponseDTO(
                tx.getId(),
                tx.getType(),
                tx.getReferenceId(),
                tx.getTransactionDate(),
                tx.getAmount(),
                tx.getCreatedBy(),
                tx.getFarmId(),
                tx.getUserId(),
                tx.getCreatedAt());
    }
}
