package com.afarms.transaction.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.afarms.transaction.model.dto.TransactionCreateRequestDTO;
import com.afarms.transaction.model.dto.TransactionCreateResponseDTO;
import com.afarms.transaction.service.DashboardService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void createIncome_validRequest_returnsCreated() {
        TransactionCreateRequestDTO request = TransactionCreateRequestDTO.builder()
                .referenceId(10L)
                .date(LocalDate.of(2026, 5, 22))
                .amount(BigDecimal.valueOf(1000))
                .createdBy("test-user")
                .farmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .type("INCOME")
                .build();

        TransactionCreateResponseDTO responseDto = new TransactionCreateResponseDTO(1L, "Transaction created");
        when(dashboardService.create(any(), any(TransactionCreateRequestDTO.class), any())).thenReturn(responseDto);

        ResponseEntity<TransactionCreateResponseDTO> response = transactionController.createIncome("******", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }
}
