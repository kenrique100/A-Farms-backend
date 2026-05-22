package com.afarms.transaction.service;

import com.afarms.transaction.model.dto.TransactionCreateRequestDTO;
import com.afarms.transaction.model.dto.TransactionCreateResponseDTO;
import com.afarms.transaction.model.entity.Transaction;
import com.afarms.transaction.repository.TransactionRepository;
import com.afarms.transaction.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

    @Test
    void shouldCreateTransactionSuccessfully() {
        // Arrange
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        DashboardServiceImpl service = new DashboardServiceImpl(transactionRepository);

        TransactionCreateRequestDTO request = TransactionCreateRequestDTO.builder()
                .type("INCOME")
                .referenceId(10L)
                .date(LocalDate.of(2026, 5, 22))
                .amount(BigDecimal.valueOf(1000))
                .createdBy("test-user")
                .farmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .build();

        Transaction savedEntity = Transaction.builder()
                .id(1L)
                .type(request.getType())
                .referenceId(request.getReferenceId())
                .transactionDate(request.getDate())
                .amount(request.getAmount())
                .createdBy(request.getCreatedBy())
                .farmId(request.getFarmId())
                .userId(request.getUserId())
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedEntity);

        // Act
        TransactionCreateResponseDTO response = service.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Transaction created", response.getMessage());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}