package com.afarms.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.afarms.transaction.model.dto.TokenValidationResponse;
import com.afarms.transaction.model.dto.TransactionCreateRequestDTO;
import com.afarms.transaction.model.dto.TransactionCreateResponseDTO;
import com.afarms.transaction.model.entity.Transaction;
import com.afarms.transaction.repository.TransactionRepository;
import com.afarms.transaction.service.impl.DashboardServiceImpl;
import com.afarms.transaction.utils.TransactionServiceUtils;
import com.afarms.transaction.utils.TransactionValidationUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DashboardServiceTest {

    @Test
    void shouldCreateTransactionSuccessfully() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        TransactionValidationUtils validationUtils = mock(TransactionValidationUtils.class);
        TransactionServiceUtils serviceUtils = mock(TransactionServiceUtils.class);
        DashboardServiceImpl service = new DashboardServiceImpl(transactionRepository, validationUtils, serviceUtils);

        UUID farmId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TransactionCreateRequestDTO request = TransactionCreateRequestDTO.builder()
                .type("INCOME")
                .referenceId(10L)
                .date(LocalDate.of(2026, 5, 22))
                .amount(BigDecimal.valueOf(1000))
                .createdBy("test-user")
                .farmId(farmId)
                .userId(userId)
                .build();

        Transaction savedEntity = Transaction.builder()
                .id(1L)
                .type("INCOME")
                .referenceId(10L)
                .transactionDate(request.getDate())
                .amount(request.getAmount())
                .createdBy(request.getCreatedBy())
                .farmId(farmId)
                .userId(userId)
                .build();

        when(serviceUtils.validateAndGetTokenInfoWithWriteAccess("******"))
                .thenReturn(new TokenValidationResponse(userId, "u", "MASTER", farmId));
        when(serviceUtils.resolveScopeFarmId(any(), any())).thenReturn(farmId);
        when(serviceUtils.findIdempotent("INCOME", 10L, farmId)).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedEntity);

        TransactionCreateResponseDTO response = service.create("******", request, "INCOME");

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }
}
