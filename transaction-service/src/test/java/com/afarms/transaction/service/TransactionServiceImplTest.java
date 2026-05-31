package com.afarms.transaction.service;

import com.afarms.transaction.client.ExpenseServiceClient;
import com.afarms.transaction.client.IncomeServiceClient;
import com.afarms.transaction.client.InvestmentServiceClient;
import com.afarms.transaction.model.dto.*;
import com.afarms.transaction.model.entity.Transaction;
import com.afarms.transaction.repository.TransactionRepository;
import com.afarms.transaction.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock private IncomeServiceClient incomeServiceClient;
    @Mock private ExpenseServiceClient expenseServiceClient;
    @Mock private InvestmentServiceClient investmentServiceClient;
    @Mock private TransactionRepository transactionRepository;

    // Use a same-thread executor so CompletableFuture runs synchronously in tests
    private final Executor syncExecutor = Runnable::run;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(
                incomeServiceClient,
                expenseServiceClient,
                investmentServiceClient,
                transactionRepository,
                syncExecutor
        );
    }

    @Test
    void getDashboardData_shouldReturnEmptyReportWhenNoDataFromAnyService() {
        when(incomeServiceClient.getIncomesByDateRange(anyString(), any(), any()))
                .thenReturn(List.of());
        when(expenseServiceClient.getExpensesByDateRange(anyString(), any(), any()))
                .thenReturn(List.of());
        when(investmentServiceClient.getAllInvestments(anyString()))
                .thenReturn(List.of());

        DashboardReportDTO result = transactionService.getDashboardData(30, "Bearer token");

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalIncome());
        assertEquals(BigDecimal.ZERO, result.getTotalExpense());
        assertEquals(BigDecimal.ZERO, result.getTotalInvestment());
        assertEquals(BigDecimal.ZERO, result.getNetGain());
        assertEquals(BigDecimal.ZERO, result.getNetLoss());
        assertTrue(result.getTransactions().isEmpty());
        verify(transactionRepository, never()).saveAll(any());
    }

    @Test
    void getDashboardData_shouldAggregateAndComputeNetGain() {
        // 1. Prepare client DTOs with description
        IncomeTransactionDTO income = new IncomeTransactionDTO(
                1L, "Corn sale", new BigDecimal("5000.00"), LocalDate.now(), "farmer");
        ExpenseTransactionDTO expense = new ExpenseTransactionDTO(
                2L, "Seeds purchase", new BigDecimal("1000.00"), LocalDate.now(), "farmer");
        InvestmentTransactionDTO investment = new InvestmentTransactionDTO(
                3L, "Tractor", new BigDecimal("500.00"), new BigDecimal("500.00"), "farmer", LocalDate.now());

        // 2. Mock client responses
        when(incomeServiceClient.getIncomesByDateRange(anyString(), any(), any()))
                .thenReturn(List.of(income));
        when(expenseServiceClient.getExpensesByDateRange(anyString(), any(), any()))
                .thenReturn(List.of(expense));
        when(investmentServiceClient.getAllInvestments(anyString()))
                .thenReturn(List.of(investment));

        // 3. Mock saved Transaction entities (include description)
        Transaction savedIncome = Transaction.builder()
                .id(1L).type("INCOME").referenceId(1L)
                .amount(new BigDecimal("5000.00")).transactionDate(LocalDate.now())
                .createdBy("farmer").description("Corn sale").build();
        Transaction savedExpense = Transaction.builder()
                .id(2L).type("EXPENSE").referenceId(2L)
                .amount(new BigDecimal("1000.00")).transactionDate(LocalDate.now())
                .createdBy("farmer").description("Seeds purchase").build();
        Transaction savedInvestment = Transaction.builder()
                .id(3L).type("INVESTMENT").referenceId(3L)
                .amount(new BigDecimal("500.00")).transactionDate(LocalDate.now())
                .createdBy("farmer").description("Tractor").build();

        when(transactionRepository.saveAll(anyList()))
                .thenReturn(List.of(savedIncome, savedExpense, savedInvestment));

        // 4. Execute service method
        DashboardReportDTO result = transactionService.getDashboardData(30, "Bearer token");

        // 5. Assert totals
        assertEquals(new BigDecimal("5000.00"), result.getTotalIncome());
        assertEquals(new BigDecimal("1000.00"), result.getTotalExpense());
        assertEquals(new BigDecimal("500.00"), result.getTotalInvestment());

        // 6. Assert net gain/loss (5000 - 1000 - 500 = 3500)
        assertEquals(new BigDecimal("3500.00"), result.getNetGain());
        assertEquals(BigDecimal.ZERO, result.getNetLoss());

        // 7. Assert transaction list size and descriptions
        assertEquals(3, result.getTransactions().size());
        assertTrue(result.getTransactions().stream()
                .anyMatch(t -> "Corn sale".equals(t.getDescription())));
        assertTrue(result.getTransactions().stream()
                .anyMatch(t -> "Seeds purchase".equals(t.getDescription())));
        assertTrue(result.getTransactions().stream()
                .anyMatch(t -> "Tractor".equals(t.getDescription())));
    }

    @Test
    void getDashboardData_shouldComputeNetLossWhenExpensesExceedIncome() {
        IncomeTransactionDTO income = new IncomeTransactionDTO(
                1L, "Corn", new BigDecimal("500.00"), LocalDate.now(), "farmer");
        ExpenseTransactionDTO expense = new ExpenseTransactionDTO(
                2L, "Equipment", new BigDecimal("2000.00"), LocalDate.now(), "farmer");

        when(incomeServiceClient.getIncomesByDateRange(anyString(), any(), any()))
                .thenReturn(List.of(income));
        when(expenseServiceClient.getExpensesByDateRange(anyString(), any(), any()))
                .thenReturn(List.of(expense));
        when(investmentServiceClient.getAllInvestments(anyString()))
                .thenReturn(List.of());

        Transaction savedIncome = Transaction.builder().id(1L).type("INCOME")
                .referenceId(1L).amount(new BigDecimal("500.00"))
                .transactionDate(LocalDate.now()).createdBy("farmer").build();
        Transaction savedExpense = Transaction.builder().id(2L).type("EXPENSE")
                .referenceId(2L).amount(new BigDecimal("2000.00"))
                .transactionDate(LocalDate.now()).createdBy("farmer").build();

        when(transactionRepository.saveAll(anyList()))
                .thenReturn(List.of(savedIncome, savedExpense));

        DashboardReportDTO result = transactionService.getDashboardData(30, "Bearer token");

        assertEquals(BigDecimal.ZERO, result.getNetGain());
        // netLoss = abs(500 - 2000) = 1500
        assertEquals(new BigDecimal("1500.00"), result.getNetLoss());
    }

    @Test
    void getDashboardData_shouldThrowTransactionExceptionOnInvalidRange() {
        assertThrows(com.afarms.transaction.exception.TransactionException.class,
                () -> transactionService.getDashboardData(0, "Bearer token"));
        assertThrows(com.afarms.transaction.exception.TransactionException.class,
                () -> transactionService.getDashboardData(-5, "Bearer token"));
    }
}