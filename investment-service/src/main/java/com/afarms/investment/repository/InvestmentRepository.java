package com.afarms.investment.repository;

import com.afarms.investment.model.entity.Investment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    Page<Investment> findByFarmId(UUID farmId, Pageable pageable);

    List<Investment> findByFarmId(UUID farmId);

    Optional<Investment> findByIdAndFarmId(Long id, UUID farmId);

    Page<Investment> findByFarmIdAndCreatedAtBetween(UUID farmId, LocalDate start,
                                                     LocalDate end, Pageable pageable);

    @Query("SELECT SUM(i.currentBalance) FROM Investment i WHERE i.farmId = :farmId")
    BigDecimal sumCurrentBalanceByFarmId(@Param("farmId") UUID farmId);
}