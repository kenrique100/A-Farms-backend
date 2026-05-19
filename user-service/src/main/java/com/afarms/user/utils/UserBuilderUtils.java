package com.afarms.user.utils;

import com.afarms.user.model.dto.MasterRegisterRequest;
import com.afarms.user.model.dto.SubUserRegisterRequest;
import com.afarms.user.model.entity.User;
import com.afarms.user.repository.AuthRepository;
import com.afarms.user.security.RoleConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserBuilderUtils {

    private final AuthRepository authRepository;

    public User createMasterUser(MasterRegisterRequest request, UUID farmId, PasswordEncoder passwordEncoder) {
        User user = User.builder()
                .email(request.getEmail())
                .username(null)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleConstants.MASTER)
                .farmId(farmId)
                .build();
        return authRepository.save(user);  // Save and return
    }

    public User createSubUser(SubUserRegisterRequest request, PasswordEncoder passwordEncoder, String role) {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .farmId(request.getFarmId())
                .build();
        return authRepository.save(user);  // Save and return
    }
}