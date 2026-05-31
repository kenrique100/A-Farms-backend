package com.afarms.transaction.service.impl;

import com.afarms.transaction.client.ExpenseServiceClient;
import com.afarms.transaction.client.IncomeServiceClient;
import com.afarms.transaction.client.InvestmentServiceClient;
import com.afarms.transaction.exception.TransactionException;
import com.afarms.transaction.model.dto.*;
import com.afarms.transaction.model.entity.Transaction;
import com.afarms.transaction.repository.TransactionRepository;
import com.afarms.transaction.service.TransactionService;
import com.afarms.transaction.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final IncomeServiceClient incomeServiceClient;
    private final ExpenseServiceClient expenseServiceClient;
    private final InvestmentServiceClient investmentServiceClient;
    private final TransactionRepository transactionRepository;
    private final Executor serviceClientExecutor;

    @Override
    public DashboardReportDTO getDashboardData(int range, String authHeader) {
        ValidationUtils.validateRange(range);

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(range);

        // Parallel fan-out to all three services
        CompletableFuture<List<IncomeTransactionDTO>> incomeFuture =
                CompletableFuture.supplyAsync(
                        () -> incomeServiceClient.getIncomesByDateRange(authHeader, start, end),
                        serviceClientExecutor);

        CompletableFuture<List<ExpenseTransactionDTO>> expenseFuture =
                CompletableFuture.supplyAsync(
                        () -> expenseServiceClient.getExpensesByDateRange(authHeader, start, end),
                        serviceClientExecutor);

        CompletableFuture<List<InvestmentTransactionDTO>> investmentFuture =
                CompletableFuture.supplyAsync(
                        () -> investmentServiceClient.getAllInvestments(authHeader),
                        serviceClientExecutor);

        CompletableFuture.allOf(incomeFuture, expenseFuture, investmentFuture).join();

        List<IncomeTransactionDTO> incomes = incomeFuture.join();
        List<ExpenseTransactionDTO> expenses = expenseFuture.join();
        List<InvestmentTransactionDTO> investments = investmentFuture.join();

        // Map each to a unified Transaction entity
        List<Transaction> transactions = new ArrayList<>();

        incomes.forEach(income -> transactions.add(Transaction.builder()
                .type("INCOME")
                .referenceId(income.getId())
                .transactionDate(income.getOccurredAt())
                .amount(income.getAmount())
                .createdBy(income.getUserName())
                .description(income.getDescription())
                .build()));

        expenses.forEach(expense -> transactions.add(Transaction.builder()
                .type("EXPENSE")
                .referenceId(expense.getId())
                .transactionDate(expense.getOccurredAt())
                .amount(expense.getAmount())
                .createdBy(expense.getUserName())
                .description(expense.getDescription())
                .build()));

        investments.forEach(investment -> transactions.add(Transaction.builder()
                .type("INVESTMENT")
                .referenceId(investment.getId())
                .transactionDate(investment.getCreatedAt() != null ? investment.getCreatedAt() : LocalDate.now())
                .amount(investment.getCurrentBalance())
                .createdBy(investment.getCreatedBy())
                .description(investment.getDescription())
                .build()));

        if (transactions.isEmpty()) {
            log.warn("No transactions found for range={} days", range);
            return new DashboardReportDTO(List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        try {
            List<Transaction> saved = transactionRepository.saveAll(transactions);

            BigDecimal totalIncome = sumByType(saved, "INCOME");
            BigDecimal totalExpense = sumByType(saved, "EXPENSE");
            BigDecimal totalInvestment = sumByType(saved, "INVESTMENT");

            BigDecimal netGainLoss = totalIncome.subtract(totalExpense).subtract(totalInvestment);
            BigDecimal netGain = netGainLoss.compareTo(BigDecimal.ZERO) > 0 ? netGainLoss : BigDecimal.ZERO;
            BigDecimal netLoss = netGainLoss.compareTo(BigDecimal.ZERO) < 0 ? netGainLoss.abs() : BigDecimal.ZERO;

            List<TransactionDTO> dtos = saved.stream()
                    .map(t -> new TransactionDTO(
                            t.getId(), t.getType(), t.getReferenceId(),
                            t.getTransactionDate(), t.getAmount(), t.getCreatedBy(), t.getDescription()))
                    .toList();

            return new DashboardReportDTO(dtos, totalIncome, totalExpense, totalInvestment, netGain, netLoss);

        } catch (Exception e) {
            log.error("Error persisting transactions or building report: {}", e.getMessage(), e);
            throw new TransactionException("Error generating dashboard data", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private BigDecimal sumByType(List<Transaction> transactions, String type) {
        return transactions.stream()
                .filter(t -> type.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}