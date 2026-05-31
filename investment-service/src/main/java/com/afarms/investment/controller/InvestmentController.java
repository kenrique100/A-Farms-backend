package com.afarms.investment.controller;

import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.service.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
@RequiredArgsConstructor
@Slf4j
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping
    public ResponseEntity<InvestmentResponseDTO> create(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody InvestmentRequestDTO request) {
        log.info("POST /api/v1/investments");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(investmentService.create(authHeader, request));
    }

    @GetMapping
    public ResponseEntity<Page<InvestmentResponseDTO>> findAll(
            @RequestHeader("Authorization") String authHeader,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("GET /api/v1/investments");
        return ResponseEntity.ok(investmentService.findAll(authHeader, pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<InvestmentResponseDTO>> findAllList(
            @RequestHeader("Authorization") String authHeader) {
        log.debug("GET /api/v1/investments/all");
        return ResponseEntity.ok(investmentService.findAllList(authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentResponseDTO> findById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        log.debug("GET /api/v1/investments/{}", id);
        return ResponseEntity.ok(investmentService.findById(authHeader, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentResponseDTO> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody InvestmentRequestDTO request) {
        log.info("PUT /api/v1/investments/{}", id);
        return ResponseEntity.ok(investmentService.update(authHeader, id, request));
    }

    @PatchMapping("/{id}/balance")
    public ResponseEntity<InvestmentResponseDTO> updateBalance(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam BigDecimal newBalance) {
        log.info("PATCH /api/v1/investments/{}/balance newBalance={}", id, newBalance);
        return ResponseEntity.ok(investmentService.updateBalance(authHeader, id, newBalance));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        log.info("DELETE /api/v1/investments/{}", id);
        investmentService.delete(authHeader, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<Page<InvestmentResponseDTO>> findByFarm(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID farmId,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("GET /api/v1/investments/farm/{}", farmId);
        return ResponseEntity.ok(investmentService.findByFarmId(authHeader, farmId, pageable));
    }

    @GetMapping("/range")
    public ResponseEntity<Page<InvestmentResponseDTO>> findByDateRange(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("GET /api/v1/investments/range?start={}&end={}", start, end);
        return ResponseEntity.ok(investmentService.findByDateRange(authHeader, start, end, pageable));
    }

    @GetMapping("/portfolio/total")
    public ResponseEntity<BigDecimal> getTotalPortfolioBalance(
            @RequestHeader("Authorization") String authHeader) {
        log.debug("GET /api/v1/investments/portfolio/total");
        return ResponseEntity.ok(investmentService.getTotalPortfolioBalance(authHeader));
    }
}