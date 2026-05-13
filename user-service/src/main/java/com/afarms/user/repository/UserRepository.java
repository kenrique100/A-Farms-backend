package com.afarms.user.repository;

import com.afarms.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrUsername(String email, String username);
    boolean existsByEmail(String email);
    boolean existsByFarmIdAndRole(UUID farmId, String role);
    List<User> findByFarmId(UUID farmId);
    List<User> findByRole(String role);
}