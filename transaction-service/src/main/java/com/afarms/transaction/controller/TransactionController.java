package com.afarms.transaction.controller;

import com.afarms.transaction.model.dto.TransactionRequestDTO;
import com.afarms.transaction.model.dto.TransactionResponseDTO;
import com.afarms.transaction.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponseDTO create(@Valid @RequestBody TransactionRequestDTO request) {
        return transactionService.create(request);
    }

    @GetMapping
    public List<TransactionResponseDTO> findAll() {
        return transactionService.findAll();
    }

    @GetMapping("/{id}")
    public TransactionResponseDTO findById(@PathVariable Long id) {
        return transactionService.findById(id);
    }
}
