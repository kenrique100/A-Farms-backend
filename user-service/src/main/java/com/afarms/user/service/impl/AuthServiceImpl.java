package com.afarms.user.service.impl;


import com.afarms.user.exception.BusinessException;
import com.afarms.user.exception.InvalidCredentialsException;
import com.afarms.user.model.dto.*;
import com.afarms.user.model.entity.User;
import com.afarms.user.repository.UserRepository;
import com.afarms.user.security.JwtUtil;
import com.afarms.user.security.RoleConstants;
import com.afarms.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.admin-key}")
    private String adminKey;

    @Override
    public String login(LoginRequest request) {
        User user = userRepository.findByEmailOrUsername(request.getEmailOrUsername(), request.getEmailOrUsername())
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
        User user = userRepository.findByEmailOrUsername(email, email)
                .orElseThrow(() -> new BusinessException("User not found"));
        return new UserDetails(user.getEmail(), user.getRole(), user.getFarmId());
    }

    @Override
    @Transactional
    public AuthResponse registerMaster(MasterRegisterRequest request) {
        if (!adminKey.equals(request.getAdminKey())) {
            throw new BusinessException("Invalid admin key");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use");
        }

        UUID farmId = UUID.randomUUID();
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("MASTER")
                .farmId(farmId)
                .build();
        User saved = userRepository.save(user);
        log.info("Master user registered for farm {} with email {}", saved.getFarmId(), saved.getEmail());
        String token = jwtUtil.generateToken(saved);
        return new AuthResponse(token, saved.getEmail(), saved.getRole(), saved.getFarmId());
    }

    @Override
    @Transactional
    public String registerSubUser(SubUserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use");
        }
        boolean farmExists = userRepository.existsByFarmIdAndRole(request.getFarmId(), "MASTER");
        if (!farmExists) {
            throw new BusinessException("Farm not found or invalid");
        }
        String role = request.getRole() == null ? RoleConstants.SUB_USER : request.getRole().trim().toUpperCase(Locale.ROOT);
        if (!RoleConstants.ALLOWED_ROLES.contains(role)) {
            throw new BusinessException("Invalid role");
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .farmId(request.getFarmId())
                .build();
        userRepository.save(user);
        log.info("Sub-user {} registered with role {} for farm {}", user.getEmail(), user.getRole(), user.getFarmId());
        return "Sub-user registered: " + user.getEmail();
    }

    @Override
    public List<UserListResponse> getUsersByFarmId(UUID farmId) {
        return userRepository.findByFarmId(farmId)
                .stream()
                .map(u -> new UserListResponse(u.getEmail(), u.getRole(), u.getFarmId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserListResponse> getAllUsers() {
        return userRepository.findAll()
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
