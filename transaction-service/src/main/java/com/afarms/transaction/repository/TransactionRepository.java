package com.afarms.transaction.repository;

import com.afarms.transaction.model.entity.Transaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFarmId(UUID farmId);

    Optional<Transaction> findByIdAndFarmId(Long id, UUID farmId);

    boolean existsBySourceServiceAndSourceReferenceId(String sourceService, Long sourceReferenceId);
}
