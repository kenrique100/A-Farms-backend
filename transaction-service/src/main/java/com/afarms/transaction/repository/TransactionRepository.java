package com.afarms.transaction.repository;

import com.afarms.transaction.model.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByTypeAndReferenceIdAndFarmId(String type, Long referenceId, UUID farmId);

    @Query("""
            select coalesce(sum(t.amount), 0)
            from Transaction t
            where t.farmId = :farmId
              and (:start is null or t.transactionDate >= :start)
              and (:end is null or t.transactionDate <= :end)
            """)
    BigDecimal sumAmountByFarmAndRange(@Param("farmId") UUID farmId,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);

    @Query("""
            select coalesce(sum(t.amount), 0)
            from Transaction t
            where t.farmId = :farmId
              and t.type = :type
              and (:start is null or t.transactionDate >= :start)
              and (:end is null or t.transactionDate <= :end)
            """)
    BigDecimal sumAmountByFarmTypeAndRange(@Param("farmId") UUID farmId,
                                           @Param("type") String type,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    @Query("""
            select count(t)
            from Transaction t
            where t.farmId = :farmId
              and (:start is null or t.transactionDate >= :start)
              and (:end is null or t.transactionDate <= :end)
            """)
    long countByFarmAndRange(@Param("farmId") UUID farmId,
                             @Param("start") LocalDate start,
                             @Param("end") LocalDate end);
}
