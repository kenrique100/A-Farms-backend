package com.afarms.user.utils;

import com.afarms.user.exception.BusinessException;
import com.afarms.user.exception.UnauthorizedException;
import com.afarms.user.model.entity.User;
import com.afarms.user.repository.AuthRepository;
import com.afarms.user.security.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserValidationUtils {

    private final AuthRepository authRepository;

    public User validateAndGetUser(UUID userId) {
        if (userId == null) {
            throw new BusinessException("User ID cannot be null");
        }
        return authRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found with ID: " + userId));
    }

    public User validateAndGetUserByEmail(String email) {
        return authRepository.findByEmailOrUsername(email, email)
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    // ← Fixed: throws UnauthorizedException instead of BusinessException
    public String validateAndExtractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        return authHeader.substring(7);
    }

    public void validateEmailUniqueness(String email, UUID currentUserId) {
        if (email == null) return;
        authRepository.findByEmail(email).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(currentUserId)) {
                throw new BusinessException("Email already in use");
            }
        });
    }

    public void validateUsernameUniqueness(String username, UUID currentUserId) {
        if (username == null) return;
        authRepository.findByUsername(username).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(currentUserId)) {
                throw new BusinessException("Username already taken");
            }
        });
    }

    public void validateEmailNotExists(String email) {
        if (email == null) return;
        if (authRepository.existsByEmail(email)) {
            throw new BusinessException("Email already in use");
        }
    }

    public void validateFarmOwnership(UUID userFarmId, UUID targetFarmId, String action) {
        if (userFarmId == null || targetFarmId == null) {
            throw new BusinessException("Invalid farm association");
        }
        if (!userFarmId.equals(targetFarmId)) {
            throw new BusinessException("You can only " + action + " users from your own farm");
        }
    }

    public void validateSubUserRole(User user) {
        if (user == null) {
            throw new BusinessException("User cannot be null");
        }
        if (RoleConstants.MASTER.equals(user.getRole())) {
            throw new BusinessException("Cannot perform this action on MASTER user");
        }
    }

    public void validateMasterCannotChangeRole(String newRole, String currentRole) {
        if (newRole != null && !newRole.equals(currentRole)) {
            throw new BusinessException("Master cannot change user role");
        }
    }

    public void validateAdminDeletion(User user) {
        if (user == null) {
            throw new BusinessException("User cannot be null");
        }
        if (RoleConstants.ADMIN.equals(user.getRole())) {
            long adminCount = authRepository.findByRole(RoleConstants.ADMIN).size();
            if (adminCount <= 1) {
                throw new BusinessException("Cannot delete the only ADMIN user");
            }
        }
    }

    public void validateRoleChange(String newRole, String requesterRole) {
        if (newRole != null) {
            if (!"ADMIN".equals(requesterRole)) {
                throw new BusinessException("Only ADMIN can change user role");
            }
            String normalizedRole = RoleConstants.normalizeRole(newRole);
            if (!RoleConstants.ALLOWED_ROLES.contains(normalizedRole)) {
                throw new BusinessException("Invalid role: " + newRole);
            }
        }
    }

    public String normalizeSubUserRole(String role) {
        String normalizedRole = role == null
                ? RoleConstants.SUB_USER
                : RoleConstants.normalizeRole(role);
        if (!RoleConstants.ALLOWED_ROLES.contains(normalizedRole)) {
            throw new BusinessException("Invalid role");
        }
        return normalizedRole;
    }

    public User extractMasterFromUsers(List<User> farmUsers) {
        return farmUsers.stream()
                .filter(u -> RoleConstants.MASTER.equals(u.getRole()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("No master found for this farm"));
    }

    // ← Fixed: modern .toList() instead of Collectors.toList()
    public List<User> extractSubUsersFromUsers(List<User> farmUsers) {
        return farmUsers.stream()
                .filter(u -> RoleConstants.SUB_USER.equals(u.getRole()))
                .toList();
    }

    public void updateUserEmail(User user, String newEmail, UUID userId) {
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            validateEmailUniqueness(newEmail, userId);
            user.setEmail(newEmail);
        }
    }

    public void updateUserUsername(User user, String newUsername, UUID userId) {
        if (newUsername != null && !newUsername.equals(user.getUsername())) {
            validateUsernameUniqueness(newUsername, userId);
            user.setUsername(newUsername);
        }
    }

    public void updateUserRole(User user, String newRole, String requesterRole) {
        if (newRole != null && !newRole.equals(user.getRole())) {
            validateRoleChange(newRole, requesterRole);
            user.setRole(RoleConstants.normalizeRole(newRole));
        }
    }

    public void updateUserRoleAdmin(User user, String newRole) {
        if (newRole != null) {
            String normalizedRole = RoleConstants.normalizeRole(newRole);
            if (!RoleConstants.ALLOWED_ROLES.contains(normalizedRole)) {
                throw new BusinessException("Invalid role: " + newRole);
            }
            user.setRole(normalizedRole);
        }
    }
}