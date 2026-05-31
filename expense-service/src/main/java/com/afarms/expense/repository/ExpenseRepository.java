package com.afarms.expense.repository;

import com.afarms.expense.model.entity.Expense;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByIdAndFarmId(Long id, UUID farmId);

    Page<Expense> findByFarmId(UUID farmId, Pageable pageable);

    Page<Expense> findByFarmIdAndUserId(UUID farmId, UUID userId, Pageable pageable);

    Page<Expense> findByFarmIdAndOccurredAtBetween(UUID farmId, LocalDate start, LocalDate end, Pageable pageable);

    Page<Expense> findByFarmIdAndUserIdAndOccurredAtBetween(
            UUID farmId, UUID userId, LocalDate start, LocalDate end, Pageable pageable);
}
