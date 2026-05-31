package com.afarms.transaction.controller;

import com.afarms.transaction.model.dto.TransactionCreateRequestDTO;
import com.afarms.transaction.model.dto.TransactionCreateResponseDTO;
import com.afarms.transaction.model.dto.TransactionResponseDTO;
import com.afarms.transaction.model.dto.TransactionSummaryDTO;
import com.afarms.transaction.service.DashboardService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final DashboardService dashboardService;

    @PostMapping("/income")
    public ResponseEntity<TransactionCreateResponseDTO> createIncome(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TransactionCreateRequestDTO request) {
        TransactionCreateResponseDTO response = dashboardService.create(authHeader, request, "INCOME");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/expense")
    public ResponseEntity<TransactionCreateResponseDTO> createExpense(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TransactionCreateRequestDTO request) {
        TransactionCreateResponseDTO response = dashboardService.create(authHeader, request, "EXPENSE");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/investment")
    public ResponseEntity<TransactionCreateResponseDTO> createInvestment(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TransactionCreateRequestDTO request) {
        TransactionCreateResponseDTO response = dashboardService.create(authHeader, request, "INVESTMENT");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponseDTO>> list(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) UUID farmId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(dashboardService.list(authHeader, farmId, userId, type, start, end, pageable));
    }

    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryDTO> summary(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) UUID farmId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(dashboardService.summary(authHeader, farmId, start, end));
    }
}
