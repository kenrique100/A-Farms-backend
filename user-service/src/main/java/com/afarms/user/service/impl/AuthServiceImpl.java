package com.afarms.user.service.impl;

import com.afarms.user.exception.InvalidCredentialsException;
import com.afarms.user.model.dto.*;
import com.afarms.user.model.entity.User;
import com.afarms.user.repository.AuthRepository;
import com.afarms.user.security.JwtUtil;
import com.afarms.user.service.AuthService;
import com.afarms.user.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserValidationUtils userValidation;
    private final FarmUtils farmUtils;
    private final UserBuilderUtils userBuilder;
    private final FarmBuilderUtils farmBuilder;
    private final ResponseBuilderUtils responseBuilder;
    private final UserDeleteUtils userDeleteUtils;

    @Override
    public String login(LoginRequest request) {
        User user = authRepository.findByEmailOrUsername(request.getEmailOrUsername(), request.getEmailOrUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for {}", request.getEmailOrUsername());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        log.info("Successful login for {} with role {}", user.getEmail(), user.getRole());
        return jwtUtil.generateToken(user);
    }

    @Override
    public UserDetails getUserDetails(String email) {
        User user = userValidation.validateAndGetUserByEmail(email);
        return responseBuilder.buildUserDetails(user);
    }

    @Override
    public TokenValidationResponse validateToken(String authHeader) {
        String token = userValidation.validateAndExtractToken(authHeader);
        jwtUtil.validateToken(token);
        return responseBuilder.buildTokenValidationResponse(token, jwtUtil);
    }

    @Override
    @Transactional
    public AuthResponse registerMaster(MasterRegisterRequest request) {
        log.info("Registering master: {}", request.getEmail());

        // Validate
        PasswordValidator.validatePasswordStrength(request.getPassword());
        userValidation.validateEmailNotExists(request.getEmail());
        farmUtils.validateFarmNameUniqueness(request.getFarmName());

        FarmBuilderUtils.FarmData farmData = farmBuilder.createNewFarm(request.getFarmName());
        User user = userBuilder.createMasterUser(request, farmData.farmId(), passwordEncoder);
        farmBuilder.linkMasterToFarm(farmData.farm(), user.getId());

        log.info("Master registered: {} for farm {}", user.getEmail(), farmData.farmName());
        return responseBuilder.buildAuthResponse(user, jwtUtil);
    }

    @Override
    @Transactional
    public String registerSubUser(SubUserRegisterRequest request) {
        log.info("Registering sub-user: {}", request.getEmail());

        // Validate
        PasswordValidator.validatePasswordStrength(request.getPassword());
        userValidation.validateEmailNotExists(request.getEmail());
        farmUtils.validateFarmExistsWithMaster(request.getFarmId());

        // Create user
        String role = userValidation.normalizeSubUserRole(request.getRole());
        User user = userBuilder.createSubUser(request, passwordEncoder, role);

        log.info("Sub-user registered: {} for farm {}", user.getEmail(), user.getFarmId());
        return responseBuilder.buildSubUserResponse(user.getEmail());
    }

    @Override
    public FarmDetailsResponse getFarmDetails(UUID farmId) {
        FarmUtils.FarmData farmData = farmUtils.getFarmWithValidation(farmId);
        List<User> farmUsers = authRepository.findByFarmId(farmId);

        User master = userValidation.extractMasterFromUsers(farmUsers);
        List<User> subUsers = userValidation.extractSubUsersFromUsers(farmUsers);

        return responseBuilder.buildFarmDetailsResponse(farmData, master, subUsers);
    }

    @Override
    public FarmsListResponse getAllFarms() {
        List<FarmUtils.FarmData> allFarms = farmUtils.getAllFarms();
        return responseBuilder.buildFarmsListResponse(allFarms, this);
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        User user = userValidation.validateAndGetUser(userId);
        return responseBuilder.buildUserResponse(user);
    }

    @Override
    public List<UserListResponse> getUsersByFarmId(UUID farmId) {
        List<User> users = authRepository.findByFarmId(farmId);
        return responseBuilder.buildUserListResponse(users);
    }

    @Override
    public List<UserListResponse> getAllUsers() {
        List<User> users = authRepository.findAll();
        return responseBuilder.buildUserListResponse(users);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request, String requesterRole) {
        User user = userValidation.validateAndGetUser(userId);

        userValidation.updateUserEmail(user, request.getEmail(), userId);
        userValidation.updateUserUsername(user, request.getUsername(), userId);
        userValidation.updateUserRole(user, request.getRole(), requesterRole);

        User updated = authRepository.save(user);
        log.info("User updated: {}", updated.getEmail());
        return responseBuilder.buildUserResponse(updated);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userValidation.validateAndGetUser(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        PasswordValidator.validatePasswordStrength(request.getNewPassword());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public UserResponse updateSubUserByMaster(UUID masterFarmId, UUID subUserId, UpdateUserRequest request) {
        User subUser = userValidation.validateAndGetUser(subUserId);

        userValidation.validateFarmOwnership(subUser.getFarmId(), masterFarmId, "update");
        userValidation.validateSubUserRole(subUser);
        userValidation.validateMasterCannotChangeRole(request.getRole(), subUser.getRole());

        userValidation.updateUserEmail(subUser, request.getEmail(), subUserId);
        userValidation.updateUserUsername(subUser, request.getUsername(), subUserId);

        User updated = authRepository.save(subUser);
        log.info("Master updated sub-user: {}", updated.getEmail());
        return responseBuilder.buildUserResponse(updated);
    }

    @Override
    @Transactional
    public UserResponse updateUserByAdmin(UUID userId, UpdateUserRequest request) {
        User user = userValidation.validateAndGetUser(userId);

        userValidation.updateUserEmail(user, request.getEmail(), userId);
        userValidation.updateUserUsername(user, request.getUsername(), userId);
        userValidation.updateUserRoleAdmin(user, request.getRole());

        User updated = authRepository.save(user);
        log.info("Admin updated user: {}", updated.getEmail());
        return responseBuilder.buildUserResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUserByAdmin(UUID userId) {
        userDeleteUtils.deleteUserAndFarmIfMaster(userId);
    }

    @Override
    @Transactional
    public void deleteSubUserByMaster(UUID masterFarmId, UUID subUserId) {
        userDeleteUtils.deleteSubUserByMaster(masterFarmId, subUserId);
    }

    @Override
    @Transactional
    public void deleteFarmAndAllUsers(UUID masterFarmId, UUID farmIdToDelete) {
        farmUtils.validateMasterFarmOwnership(masterFarmId, farmIdToDelete);
        farmUtils.deleteFarmAndAllUsers(farmIdToDelete, "Master");
    }

    @Override
    @Transactional
    public void deleteFarmByAdmin(UUID farmId) {
        farmUtils.deleteFarmAndAllUsers(farmId, "Admin");
    }

    @Override
    @Transactional
    public int deleteAllSubUsers() {
        return userDeleteUtils.deleteAllSubUsers();
    }
}