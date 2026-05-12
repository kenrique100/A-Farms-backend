package com.afarms.investment.controller;

import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.service.InvestmentService;
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
@RequestMapping("/api/v1/investments")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvestmentResponseDTO create(@Valid @RequestBody InvestmentRequestDTO request) {
        return investmentService.create(request);
    }

    @GetMapping
    public List<InvestmentResponseDTO> findAll() {
        return investmentService.findAll();
    }

    @GetMapping("/{id}")
    public InvestmentResponseDTO findById(@PathVariable Long id) {
        return investmentService.findById(id);
    }
}
