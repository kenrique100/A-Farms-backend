package com.afarms.user.utils;

import com.afarms.user.model.entity.User;
import com.afarms.user.repository.AuthRepository;
import com.afarms.user.security.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDeleteUtils {

    private final AuthRepository authRepository;
    private final UserValidationUtils userValidation;
    private final FarmUtils farmUtils;

    public void deleteUserAndFarmIfMaster(UUID userId) {
        User user = userValidation.validateAndGetUser(userId);
        userValidation.validateAdminDeletion(user);

        if (RoleConstants.MASTER.equals(user.getRole()) && user.getFarmId() != null) {
            farmUtils.deleteFarmAndAllUsers(user.getFarmId(), "Admin");
        }

        authRepository.delete(user);
        log.info("Admin deleted user: {} with role {}", user.getEmail(), user.getRole());
    }

    public void deleteSubUserByMaster(UUID masterFarmId, UUID subUserId) {
        User subUser = userValidation.validateAndGetUser(subUserId);

        userValidation.validateFarmOwnership(subUser.getFarmId(), masterFarmId, "delete");
        userValidation.validateSubUserRole(subUser);

        authRepository.delete(subUser);
        log.info("Master deleted sub-user: {} from farm {}", subUser.getEmail(), masterFarmId);
    }

    public int deleteAllSubUsers() {
        java.util.List<User> allSubUsers = authRepository.findByRole(RoleConstants.SUB_USER);
        int count = allSubUsers.size();

        for (User subUser : allSubUsers) {
            authRepository.delete(subUser);
            log.info("Deleted sub-user: {}", subUser.getEmail());
        }

        log.info("Admin deleted all {} sub-users from the system", count);
        return count;
    }
}