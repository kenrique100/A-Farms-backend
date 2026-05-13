package com.afarms.user.service;

import com.afarms.user.model.dto.*;

import java.util.List;
import java.util.UUID;

public interface AuthService {
    String login(LoginRequest request);
    UserDetails getUserDetails(String email);
    AuthResponse registerMaster(MasterRegisterRequest request);
    String registerSubUser(SubUserRegisterRequest request);
    List<UserListResponse> getUsersByFarmId(UUID farmId);
    List<UserListResponse> getAllUsers();
    TokenValidationResponse validateToken(String authHeader);
}