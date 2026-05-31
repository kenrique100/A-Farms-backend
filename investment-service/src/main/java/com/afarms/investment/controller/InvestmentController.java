package com.afarms.investment.controller;

import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.service.InvestmentService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping
    public ResponseEntity<InvestmentResponseDTO> create(@RequestHeader("Authorization") String authHeader,
                                                        @Valid @RequestBody InvestmentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investmentService.create(authHeader, request));
    }

    @GetMapping
    public ResponseEntity<Page<InvestmentResponseDTO>> findAll(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) UUID farmId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(investmentService.findAll(authHeader, farmId, userId, start, end, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentResponseDTO> findById(@RequestHeader("Authorization") String authHeader,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(investmentService.findById(authHeader, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentResponseDTO> update(@RequestHeader("Authorization") String authHeader,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody InvestmentRequestDTO request) {
        return ResponseEntity.ok(investmentService.update(authHeader, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable Long id) {
        investmentService.delete(authHeader, id);
        return ResponseEntity.noContent().build();
    }
}
