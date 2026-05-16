package com.afarms.user.utils;

import com.afarms.user.exception.BusinessException;
import com.afarms.user.model.entity.Farm;
import com.afarms.user.model.entity.User;
import com.afarms.user.repository.AuthRepository;
import com.afarms.user.repository.FarmRepository;
import com.afarms.user.security.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FarmUtils {

    private final FarmRepository farmRepository;
    private final AuthRepository authRepository;

    public record FarmData(Farm farm, UUID farmId, String farmName) {}

    public FarmData getFarmWithValidation(UUID farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new BusinessException("Farm not found with ID: " + farmId));
        return new FarmData(farm, farm.getId(), farm.getName());
    }

    public void validateFarmNameUniqueness(String farmName) {
        if (farmRepository.existsByName(farmName)) {
            throw new BusinessException("Farm name already taken");
        }
    }

    public void validateFarmExistsWithMaster(UUID farmId) {
        boolean farmExists = authRepository.existsByFarmIdAndRole(farmId, RoleConstants.MASTER);
        if (!farmExists) {
            throw new BusinessException("Farm not found or invalid");
        }
    }

    public void validateMasterFarmOwnership(UUID masterFarmId, UUID targetFarmId) {
        if (!masterFarmId.equals(targetFarmId)) {
            throw new BusinessException("You can only delete your own farm");
        }
    }

    public void deleteFarmAndAllUsers(UUID farmId, String deletedBy) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new BusinessException("Farm not found with ID: " + farmId));

        List<User> farmUsers = authRepository.findByFarmId(farmId);

        for (User user : farmUsers) {
            authRepository.delete(user);
            log.info("{} deleted user: {} from farm {}", deletedBy, user.getEmail(), farmId);
        }

        farmRepository.delete(farm);
        log.info("{} deleted farm: {} with all {} users", deletedBy, farm.getName(), farmUsers.size());
    }

    public List<FarmData> getAllFarms() {
        return farmRepository.findAll().stream()
                .map(farm -> new FarmData(farm, farm.getId(), farm.getName()))
                .collect(java.util.stream.Collectors.toList());
    }
}