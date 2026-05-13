package com.afarms.user.model.mapper;

import com.afarms.user.model.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(UserRequestDTO request) {
        User entity = new User();
        entity.setDescription(request.getDescription());
        entity.setAmount(request.getAmount());
        entity.setOccurredAt(request.getOccurredAt());
        return entity;
    }

    public static UserResponseDTO toResponse(User entity) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(entity.getId());
        response.setDescription(entity.getDescription());
        response.setAmount(entity.getAmount());
        response.setOccurredAt(entity.getOccurredAt());
        return response;
    }
}
