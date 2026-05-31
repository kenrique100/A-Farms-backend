package com.afarms.expense.controller;

import com.afarms.expense.model.dto.ExpenseRequestDTO;
import com.afarms.expense.model.dto.ExpenseResponseDTO;
import com.afarms.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> create(@RequestHeader("Authorization") String authHeader,
                                                     @Valid @RequestBody ExpenseRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.create(authHeader, request));
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseResponseDTO>> findAll(
            @RequestHeader("Authorization") String authHeader,
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(expenseService.findAll(authHeader, pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ExpenseResponseDTO>> findAllList(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(expenseService.findAllList(authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> findById(@RequestHeader("Authorization") String authHeader,
                                                       @PathVariable Long id) {
        return ResponseEntity.ok(expenseService.findById(authHeader, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> update(@RequestHeader("Authorization") String authHeader,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody ExpenseRequestDTO request) {
        return ResponseEntity.ok(expenseService.update(authHeader, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable Long id) {
        expenseService.delete(authHeader, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<Page<ExpenseResponseDTO>> findByFarm(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID farmId,
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(expenseService.findByFarmId(authHeader, farmId, pageable));
    }

    @GetMapping("/range")
    public ResponseEntity<Page<ExpenseResponseDTO>> findByDateRange(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(expenseService.findByDateRange(authHeader, start, end, pageable));
    }
}