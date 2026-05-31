package com.afarms.investment.repository;

import com.afarms.investment.model.entity.Investment;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    Optional<Investment> findByIdAndFarmId(Long id, UUID farmId);

    Page<Investment> findByFarmId(UUID farmId, Pageable pageable);

    Page<Investment> findByFarmIdAndUserId(UUID farmId, UUID userId, Pageable pageable);

    Page<Investment> findByFarmIdAndOccurredAtBetween(UUID farmId, LocalDate start, LocalDate end, Pageable pageable);

    Page<Investment> findByFarmIdAndUserIdAndOccurredAtBetween(
            UUID farmId, UUID userId, LocalDate start, LocalDate end, Pageable pageable);
}
