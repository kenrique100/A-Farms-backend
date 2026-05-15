package com.afarms.user.service.impl;

import com.afarms.user.exception.BusinessException;
import com.afarms.user.exception.InvalidCredentialsException;
import com.afarms.user.model.dto.*;
import com.afarms.user.model.entity.Farm;
import com.afarms.user.model.entity.User;
import com.afarms.user.repository.AuthRepository;
import com.afarms.user.repository.FarmRepository;
import com.afarms.user.security.JwtUtil;
import com.afarms.user.security.RoleConstants;
import com.afarms.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final FarmRepository farmRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
        User user = authRepository.findByEmailOrUsername(email, email)
                .orElseThrow(() -> new BusinessException("User not found"));
        return new UserDetails(user.getEmail(), user.getRole(), user.getFarmId());
    }

    @Override
    @Transactional
    public AuthResponse registerMaster(MasterRegisterRequest request) {
        log.info("Attempting to register master user with email: {}", request.getEmail());

        // 1. Email uniqueness
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use");
        }

        // 2. Farm name uniqueness
        if (farmRepository.existsByName(request.getFarmName())) {
            throw new BusinessException("Farm name already taken");
        }

        Farm farm = Farm.builder()
                .name(request.getFarmName())
                .masterId(null)
                .build();
        Farm savedFarm = farmRepository.save(farm);
        log.info("Created farm with ID: {}", savedFarm.getId());

        User user = User.builder()
                .email(request.getEmail())
                .username(null)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleConstants.MASTER)
                .farmId(savedFarm.getId())
                .build();
        User savedUser = authRepository.save(user);
        log.info("Created master user with ID: {}", savedUser.getId());

        // 5. Update farm with the master's user ID
        savedFarm.setMasterId(savedUser.getId());
        farmRepository.save(savedFarm);

        log.info("Master registered successfully: {} for farm '{}' (id={})",
                savedUser.getEmail(), savedFarm.getName(), savedFarm.getId());

        String token = jwtUtil.generateToken(savedUser);
        return new AuthResponse(token, savedUser.getEmail(), savedUser.getRole(), savedUser.getFarmId());
    }

    @Override
    @Transactional
    public String registerSubUser(SubUserRegisterRequest request) {
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use");
        }

        // Verify that the farm exists and has a MASTER
        boolean farmExists = authRepository.existsByFarmIdAndRole(request.getFarmId(), RoleConstants.MASTER);
        if (!farmExists) {
            throw new BusinessException("Farm not found or invalid");
        }

        String role = request.getRole() == null ? RoleConstants.SUB_USER : RoleConstants.normalizeRole(request.getRole());
        if (!RoleConstants.ALLOWED_ROLES.contains(role)) {
            throw new BusinessException("Invalid role");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .farmId(request.getFarmId())
                .build();
        authRepository.save(user);

        log.info("Sub-user {} registered with role {} for farm {}", user.getEmail(), user.getRole(), user.getFarmId());
        return "Sub-user registered: " + user.getEmail();
    }

    @Override
    public List<UserListResponse> getUsersByFarmId(UUID farmId) {
        return authRepository.findByFarmId(farmId)
                .stream()
                .map(u -> new UserListResponse(u.getEmail(), u.getRole(), u.getFarmId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserListResponse> getAllUsers() {
        return authRepository.findAll()
                .stream()
                .map(u -> new UserListResponse(u.getEmail(), u.getRole(), u.getFarmId()))
                .collect(Collectors.toList());
    }

    @Override
    public TokenValidationResponse validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException("Invalid JWT token");
        }
        return new TokenValidationResponse(
                jwtUtil.extractUserId(token),
                jwtUtil.extractFarmId(token),
                jwtUtil.extractRole(token)
        );
    }
}