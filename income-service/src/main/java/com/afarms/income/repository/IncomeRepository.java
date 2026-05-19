package com.afarms.income.repository;

import com.afarms.income.model.entity.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    Page<Income> findByFarmId(UUID farmId, Pageable pageable);
    List<Income> findByFarmId(UUID farmId);
    Optional<Income> findByIdAndFarmId(Long id, UUID farmId);
    Page<Income> findByFarmIdAndOccurredAtBetween(UUID farmId, LocalDate start, LocalDate end, Pageable pageable);
}