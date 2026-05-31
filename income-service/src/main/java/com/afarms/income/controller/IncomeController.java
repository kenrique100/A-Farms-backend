package com.afarms.income.controller;

import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.IncomeResponseDTO;
import com.afarms.income.service.IncomeService;
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
@RequestMapping("/api/v1/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeResponseDTO> create(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody IncomeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(incomeService.create(authHeader, request));
    }

    @GetMapping
    public ResponseEntity<Page<IncomeResponseDTO>> findAll(
            @RequestHeader("Authorization") String authHeader,
            @PageableDefault(size = 20, sort = "occurredAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(incomeService.findAll(authHeader, pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<IncomeResponseDTO>> findAllList(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(incomeService.findAllList(authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponseDTO> findById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        return ResponseEntity.ok(incomeService.findById(authHeader, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponseDTO> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody IncomeRequestDTO request) {
        return ResponseEntity.ok(incomeService.update(authHeader, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        incomeService.delete(authHeader, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<Page<IncomeResponseDTO>> findByFarm(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID farmId,
            @PageableDefault(size = 20, sort = "occurredAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(incomeService.findByFarmId(authHeader, farmId, pageable));
    }

    @GetMapping("/range")
    public ResponseEntity<Page<IncomeResponseDTO>> findByDateRange(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 20, sort = "occurredAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(incomeService.findByDateRange(authHeader, start, end, pageable));
    }
}