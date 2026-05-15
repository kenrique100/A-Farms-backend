package com.afarms.user.repository;

import com.afarms.user.model.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FarmRepository extends JpaRepository<Farm, UUID> {
    Optional<Farm> findByName(String name);
    boolean existsByName(String name);
}