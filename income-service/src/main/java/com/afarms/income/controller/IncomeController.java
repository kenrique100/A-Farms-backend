package com.afarms.income.controller;

import com.afarms.income.model.dto.IncomeRequestDTO;
import com.afarms.income.model.dto.IncomeResponseDTO;
import com.afarms.income.service.IncomeService;
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
@RequestMapping("/api/v1/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncomeResponseDTO create(@Valid @RequestBody IncomeRequestDTO request) {
        return incomeService.create(request);
    }

    @GetMapping
    public List<IncomeResponseDTO> findAll() {
        return incomeService.findAll();
    }

    @GetMapping("/{id}")
    public IncomeResponseDTO findById(@PathVariable Long id) {
        return incomeService.findById(id);
    }
}
