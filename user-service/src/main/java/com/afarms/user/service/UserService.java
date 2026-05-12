package com.afarms.user.service;

import com.afarms.user.model.dto.UserRequestDTO;
import com.afarms.user.model.dto.UserResponseDTO;
import java.util.List;

public interface UserService {

    UserResponseDTO create(UserRequestDTO request);

    List<UserResponseDTO> findAll();

    UserResponseDTO findById(Long id);
}
