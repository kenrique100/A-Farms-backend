package com.afarms.transaction.controller;

import com.afarms.transaction.model.dto.TransactionCreateRequest;
import com.afarms.transaction.model.dto.TransactionCreateResponse;
import com.afarms.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions/internal")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/income")
    public ResponseEntity<TransactionCreateResponse> createIncomeTransaction(
            @RequestHeader("X-Internal-Api-Key") String apiKey,
            @Valid @RequestBody TransactionCreateRequest request) {
        log.debug("Internal request to create income transaction for farm {}", request.getFarmId());
        TransactionCreateResponse response = transactionService.createIncomeTransaction(request, apiKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ----- Dashboard endpoint (partially commented) -----
    // @GetMapping("/dashboard")
    // public ResponseEntity<DashboardReportDTO> getDashboardData(@RequestParam int range) {
    //     // Implementation would call income/expense/investment services.
    //     // For now, return empty or throw unsupported.
    //     throw new UnsupportedOperationException("Dashboard not yet implemented");
    // }
}