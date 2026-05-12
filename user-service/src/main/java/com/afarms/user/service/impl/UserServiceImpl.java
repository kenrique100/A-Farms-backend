package com.afarms.user.service.impl;

import com.afarms.user.exception.ResourceNotFoundException;
import com.afarms.user.model.dto.UserRequestDTO;
import com.afarms.user.model.dto.UserResponseDTO;
import com.afarms.user.model.entity.User;
import com.afarms.user.model.mapper.UserMapper;
import com.afarms.user.repository.UserRepository;
import com.afarms.user.service.UserService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponseDTO create(UserRequestDTO request) {
        User entity = UserMapper.toEntity(request);
        return UserMapper.toResponse(userRepository.save(entity));
    }

    @Override
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponseDTO findById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for id " + id));
    }
}
