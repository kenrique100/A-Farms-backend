package com.afarms.user.utils;

import com.afarms.user.exception.BusinessException;
import com.afarms.user.model.dto.UpdateUserRequest;
import com.afarms.user.model.entity.User;
import com.afarms.user.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserUpdateUtils {

    private final AuthRepository authRepository;
    private final UserValidationUtils userValidation;

    public User applyBasicUpdates(UUID userId, UpdateUserRequest request, String requesterRole) {
        User user = userValidation.validateAndGetUser(userId);

        userValidation.updateUserEmail(user, request.getEmail(), userId);
        userValidation.updateUserUsername(user, request.getUsername(), userId);
        userValidation.updateUserRole(user, request.getRole(), requesterRole);

        return authRepository.save(user);
    }

    public User applyMasterSubUserUpdate(UUID subUserId, UUID masterFarmId, UpdateUserRequest request) {
        User subUser = userValidation.validateAndGetUser(subUserId);

        userValidation.validateFarmOwnership(subUser.getFarmId(), masterFarmId, "update");
        userValidation.validateSubUserRole(subUser);

        if (request.getRole() != null && !request.getRole().equals(subUser.getRole())) {
            throw new BusinessException("Master cannot change user role");
        }

        userValidation.updateUserEmail(subUser, request.getEmail(), subUserId);
        userValidation.updateUserUsername(subUser, request.getUsername(), subUserId);

        return authRepository.save(subUser);
    }

    public User applyAdminUpdate(UUID userId, UpdateUserRequest request) {
        User user = userValidation.validateAndGetUser(userId);

        userValidation.updateUserEmail(user, request.getEmail(), userId);
        userValidation.updateUserUsername(user, request.getUsername(), userId);
        userValidation.updateUserRoleAdmin(user, request.getRole());

        return authRepository.save(user);
    }
}