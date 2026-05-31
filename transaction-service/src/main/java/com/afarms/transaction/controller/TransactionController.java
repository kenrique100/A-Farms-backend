package com.afarms.transaction.controller;

import com.afarms.transaction.model.dto.DashboardReportDTO;
import com.afarms.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardReportDTO> getDashboardData(
            @RequestParam int range,
            @RequestHeader("Authorization") String authHeader) {
        log.info("Dashboard request for range={} days", range);
        return ResponseEntity.ok(transactionService.getDashboardData(range, authHeader));
    }
}