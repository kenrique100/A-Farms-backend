package com.afarms.user.controller;

import com.afarms.user.model.dto.*;
import com.afarms.user.security.RoleConstants;
import com.afarms.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        UserDetails userDetails = authService.getUserDetails(request.getEmailOrUsername());
        return ResponseEntity.ok(new AuthResponse(token, userDetails.getEmail(), userDetails.getRole(), userDetails.getFarmId()));
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.validateToken(authHeader));
    }

    @PostMapping("/register/master")
    public ResponseEntity<AuthResponse> registerMaster(@Valid @RequestBody MasterRegisterRequest request) {
        return ResponseEntity.ok(authService.registerMaster(request));
    }

    @PostMapping("/register/sub-user")
    public ResponseEntity<String> registerSubUser(@Valid @RequestBody SubUserRegisterRequest request) {
        return ResponseEntity.ok(authService.registerSubUser(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        log.debug("Getting current user from token");
        TokenValidationResponse tokenInfo = authService.validateToken(authHeader);
        log.debug("Token info - userId: {}, role: {}", tokenInfo.getUserId(), tokenInfo.getRole());

        if (tokenInfo.getUserId() == null) {
            log.error("User ID is null in token");
            return ResponseEntity.status(401).build();
        }

        UserResponse user = authService.getUserById(tokenInfo.getUserId());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId) {
        TokenValidationResponse tokenInfo = getTokenInfo(authHeader);
        // Only admin or the user themselves can view user details
        if (!hasRole(tokenInfo, RoleConstants.ADMIN) && !tokenInfo.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @GetMapping("/farm/users")
    public ResponseEntity<List<UserListResponse>> getFarmUsers(@RequestHeader("Authorization") String authHeader) {
        TokenValidationResponse tokenInfo = getTokenInfo(authHeader);
        if (!hasRole(tokenInfo, RoleConstants.MASTER)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(authService.getUsersByFarmId(tokenInfo.getFarmId()));
    }

    @GetMapping("/admin/all-users")
    public ResponseEntity<List<UserListResponse>> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        TokenValidationResponse tokenInfo = getTokenInfo(authHeader);
        if (!hasRole(tokenInfo, RoleConstants.ADMIN)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateUserRequest request) {
        TokenValidationResponse tokenInfo = authService.validateToken(authHeader);
        UserResponse updated = authService.updateUser(tokenInfo.getUserId(), request, tokenInfo.getRole());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        TokenValidationResponse tokenInfo = authService.validateToken(authHeader);
        authService.changePassword(tokenInfo.getUserId(), request);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PutMapping("/master/sub-user/{subUserId}")
    public ResponseEntity<UserResponse> updateSubUserByMaster(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID subUserId,
            @Valid @RequestBody UpdateUserRequest request) {
        TokenValidationResponse tokenInfo = getTokenInfo(authHeader);
        if (!hasRole(tokenInfo, RoleConstants.MASTER)) {
            return ResponseEntity.status(403).build();
        }
        UserResponse updated = authService.updateSubUserByMaster(tokenInfo.getFarmId(), subUserId, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/admin/user/{userId}")
    public ResponseEntity<UserResponse> updateUserByAdmin(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        TokenValidationResponse tokenInfo = getTokenInfo(authHeader);
        if (!hasRole(tokenInfo, RoleConstants.ADMIN)) {
            return ResponseEntity.status(403).build();
        }
        UserResponse updated = authService.updateUserByAdmin(userId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteCurrentUser(@RequestHeader("Authorization") String authHeader) {
        TokenValidationResponse tokenInfo = authService.validateToken(authHeader);
        authService.deleteUserByAdmin(tokenInfo.getUserId());
        return ResponseEntity.ok("Your account has been deleted");
    }

    @DeleteMapping("/admin/user/{userId}")
    public ResponseEntity<String> deleteUserByAdmin(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId) {
        TokenValidationResponse tokenInfo = getTokenInfo(authHeader);
        if (!hasRole(tokenInfo, RoleConstants.ADMIN)) {
            return ResponseEntity.status(403).body("Only ADMIN can delete users");
        }
        authService.deleteUserByAdmin(userId);
        return ResponseEntity.ok("User deleted successfully by ADMIN");
    }

    @DeleteMapping("/master/sub-user/{subUserId}")
    public ResponseEntity<String> deleteSubUserByMaster(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID subUserId) {
        TokenValidationResponse tokenInfo = getTokenInfo(authHeader);
        if (!hasRole(tokenInfo, RoleConstants.MASTER)) {
            return ResponseEntity.status(403).body("Only MASTER can delete sub-users");
        }
        authService.deleteSubUserByMaster(tokenInfo.getFarmId(), subUserId);
        return ResponseEntity.ok("Sub-user deleted successfully");
    }

    @DeleteMapping("/master/farm")
    public ResponseEntity<String> deleteMyFarm(@RequestHeader("Authorization") String authHeader) {
        TokenValidationResponse tokenInfo = getTokenInfo(authHeader);
        if (!hasRole(tokenInfo, RoleConstants.MASTER)) {
            return ResponseEntity.status(403).body("Only MASTER can delete farms");
        }
        authService.deleteFarmAndAllUsers(tokenInfo.getFarmId(), tokenInfo.getFarmId());
        return ResponseEntity.ok("Your farm and all associated users deleted successfully");
    }

    @DeleteMapping("/admin/farm/{farmId}")
    public ResponseEntity<String> deleteFarmByAdmin(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID farmId) {
        TokenValidationResponse tokenInfo = getTokenInfo(authHeader);
        if (!hasRole(tokenInfo, RoleConstants.ADMIN)) {
            return ResponseEntity.status(403).body("Only ADMIN can delete farms");
        }
        authService.deleteFarmByAdmin(farmId);
        return ResponseEntity.ok("Farm deleted successfully by ADMIN");
    }

    @DeleteMapping("/admin/delete-all-sub-users")
    public ResponseEntity<String> deleteAllSubUsers(@RequestHeader("Authorization") String authHeader) {
        TokenValidationResponse tokenInfo = getTokenInfo(authHeader);
        if (!hasRole(tokenInfo, RoleConstants.ADMIN)) {
            return ResponseEntity.status(403).body("Only ADMIN can perform this action");
        }
        int deletedCount = authService.deleteAllSubUsers();
        return ResponseEntity.ok("Deleted " + deletedCount + " sub-users successfully");
    }

    @GetMapping("/farm/details")
    public ResponseEntity<FarmDetailsResponse> getFarmDetails(@RequestHeader("Authorization") String authHeader) {
        TokenValidationResponse tokenInfo = getTokenInfo(authHeader);
        if (!hasRole(tokenInfo, RoleConstants.MASTER)) {
            return ResponseEntity.status(403).build();
        }
        FarmDetailsResponse farmDetails = authService.getFarmDetails(tokenInfo.getFarmId());
        return ResponseEntity.ok(farmDetails);
    }

    @GetMapping("/admin/all-farms")
    public ResponseEntity<FarmsListResponse> getAllFarms(@RequestHeader("Authorization") String authHeader) {
        TokenValidationResponse tokenInfo = getTokenInfo(authHeader);
        if (!hasRole(tokenInfo, RoleConstants.ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        FarmsListResponse allFarms = authService.getAllFarms();
        return ResponseEntity.ok(allFarms);
    }

    private TokenValidationResponse getTokenInfo(String authHeader) {
        return authService.validateToken(authHeader);
    }

    private boolean hasRole(TokenValidationResponse tokenInfo, String role) {
        return role.equals(tokenInfo.getRole());
    }
}
