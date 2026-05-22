package com.afarms.transaction.controller;

import com.afarms.transaction.model.dto.TransactionCreateRequestDTO;
import com.afarms.transaction.model.dto.TransactionCreateResponseDTO;
import com.afarms.transaction.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final DashboardService dashboardService;

    @PostMapping("/income")
    public ResponseEntity<TransactionCreateResponseDTO> createIncome(
            @RequestBody TransactionCreateRequestDTO request) {
        request.setType("INCOME");

        TransactionCreateResponseDTO response = dashboardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}