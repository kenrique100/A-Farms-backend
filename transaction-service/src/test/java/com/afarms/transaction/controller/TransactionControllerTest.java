package com.afarms.transaction.controller;

import com.afarms.transaction.model.dto.DashboardReportDTO;
import com.afarms.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void getDashboardData_shouldReturnOkWithReport() {
        String authHeader = "Bearer test-token";
        int range = 30;

        DashboardReportDTO mockReport = new DashboardReportDTO(
                List.of(),
                new BigDecimal("5000.00"),
                new BigDecimal("2000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("2000.00"),
                BigDecimal.ZERO
        );

        when(transactionService.getDashboardData(range, authHeader)).thenReturn(mockReport);

        ResponseEntity<DashboardReportDTO> response =
                transactionController.getDashboardData(range, authHeader);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(new BigDecimal("5000.00"), response.getBody().getTotalIncome());
        assertEquals(new BigDecimal("2000.00"), response.getBody().getNetGain());
        verify(transactionService, times(1)).getDashboardData(range, authHeader);
    }

    @Test
    void getDashboardData_shouldDelegateToServiceWithCorrectArguments() {
        String authHeader = "Bearer another-token";
        int range = 7;

        when(transactionService.getDashboardData(range, authHeader))
                .thenReturn(new DashboardReportDTO(
                        List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        transactionController.getDashboardData(range, authHeader);

        verify(transactionService).getDashboardData(7, "Bearer another-token");
    }
}