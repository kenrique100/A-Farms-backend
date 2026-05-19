package com.afarms.user.service;

import com.afarms.user.model.dto.*;

import java.util.List;
import java.util.UUID;

public interface AuthService {
    String login(LoginRequest request);
    UserDetails getUserDetails(String email);
    TokenValidationResponse validateToken(String authHeader);
    AuthResponse registerMaster(MasterRegisterRequest request);
    String registerSubUser(SubUserRegisterRequest request);
    UserResponse getUserById(UUID userId);
    List<UserListResponse> getUsersByFarmId(UUID farmId);
    List<UserListResponse> getAllUsers();
    FarmDetailsResponse getFarmDetails(UUID farmId);  // NEW METHOD
    UserResponse updateUser(UUID userId, UpdateUserRequest request, String requesterRole);
    void changePassword(UUID userId, ChangePasswordRequest request);
    UserResponse updateSubUserByMaster(UUID masterFarmId, UUID subUserId, UpdateUserRequest request);
    UserResponse updateUserByAdmin(UUID userId, UpdateUserRequest request);
    void deleteUserByAdmin(UUID userId);
    void deleteSubUserByMaster(UUID masterFarmId, UUID subUserId);
    void deleteFarmAndAllUsers(UUID masterFarmId, UUID farmIdToDelete);
    void deleteFarmByAdmin(UUID farmId);
    int deleteAllSubUsers();
    FarmsListResponse getAllFarms();
}