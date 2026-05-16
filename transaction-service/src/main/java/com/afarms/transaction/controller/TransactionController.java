package com.afarms.transaction.controller;

import com.afarms.transaction.model.dto.InternalIncomeTransactionRequestDTO;
import com.afarms.transaction.model.dto.TransactionRequestDTO;
import com.afarms.transaction.model.dto.TransactionResponseDTO;
import com.afarms.transaction.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> create(@RequestHeader("Authorization") String authHeader,
                                                         @Valid @RequestBody TransactionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(authHeader, request));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> findAll(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(transactionService.findAll(authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> findById(@RequestHeader("Authorization") String authHeader,
                                                           @PathVariable Long id) {
        return ResponseEntity.ok(transactionService.findById(authHeader, id));
    }

    @PostMapping("/internal/income")
    public ResponseEntity<TransactionResponseDTO> createIncomeTransaction(
            @RequestHeader("X-Internal-Api-Key") String internalApiKey,
            @Valid @RequestBody InternalIncomeTransactionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createIncomeTransaction(internalApiKey, request));
    }
}
