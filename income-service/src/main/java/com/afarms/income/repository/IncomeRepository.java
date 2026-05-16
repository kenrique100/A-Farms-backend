package com.afarms.income.repository;

import com.afarms.income.model.entity.Income;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByFarmId(UUID farmId);

    Optional<Income> findByIdAndFarmId(Long id, UUID farmId);
}
