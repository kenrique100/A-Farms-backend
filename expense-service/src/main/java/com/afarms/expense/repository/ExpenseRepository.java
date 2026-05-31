package com.afarms.expense.repository;

import com.afarms.expense.model.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Page<Expense> findByFarmId(UUID farmId, Pageable pageable);
    List<Expense> findByFarmId(UUID farmId);
    Optional<Expense> findByIdAndFarmId(Long id, UUID farmId);
    Page<Expense> findByFarmIdAndOccurredAtBetween(UUID farmId, LocalDate start, LocalDate end, Pageable pageable);
}