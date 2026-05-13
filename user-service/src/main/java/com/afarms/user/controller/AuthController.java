package com.afarms.user.controller;

import com.afarms.user.model.dto.*;
import com.afarms.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        UserDetails userDetails = authService.getUserDetails(request.getEmailOrUsername());
        return ResponseEntity.ok(new AuthResponse(token, userDetails.getEmail(), userDetails.getRole(), userDetails.getFarmId()));
    }

    @PostMapping("/register/master")
    public ResponseEntity<AuthResponse> registerMaster(@Valid @RequestBody MasterRegisterRequest request) {
        return ResponseEntity.ok(authService.registerMaster(request));
    }

    @PostMapping("/register/sub-user")
    public ResponseEntity<String> registerSubUser(@Valid @RequestBody SubUserRegisterRequest request) {
        return ResponseEntity.ok(authService.registerSubUser(request));
    }

    @GetMapping("/farm/users")
    public ResponseEntity<List<UserListResponse>> getFarmUsers(@RequestHeader("Authorization") String authHeader) {
        TokenValidationResponse tokenInfo = authService.validateToken(authHeader);
        if (!"MASTER".equals(tokenInfo.getRole())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(authService.getUsersByFarmId(tokenInfo.getFarmId()));
    }

    @GetMapping("/admin/all-users")
    public ResponseEntity<List<UserListResponse>> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        TokenValidationResponse tokenInfo = authService.validateToken(authHeader);
        if (!"ADMIN".equals(tokenInfo.getRole())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.validateToken(authHeader));
    }
}