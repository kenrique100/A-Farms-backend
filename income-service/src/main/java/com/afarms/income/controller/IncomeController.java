package com.afarms.income.controller;

import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.IncomeResponseDTO;
import com.afarms.income.service.IncomeService;
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
@RequestMapping("/api/v1/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping
    public ResponseEntity<IncomeResponseDTO> create(@RequestHeader("Authorization") String authHeader,
                                                    @Valid @RequestBody IncomeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incomeService.create(authHeader, request));
    }

    @GetMapping
    public ResponseEntity<List<IncomeResponseDTO>> findAll(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(incomeService.findAll(authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponseDTO> findById(@RequestHeader("Authorization") String authHeader,
                                                       @PathVariable Long id) {
        return ResponseEntity.ok(incomeService.findById(authHeader, id));
    }
}
