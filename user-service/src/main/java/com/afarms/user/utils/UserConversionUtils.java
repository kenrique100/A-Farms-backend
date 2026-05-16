package com.afarms.user.utils;

import com.afarms.user.model.dto.UserListResponse;
import com.afarms.user.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserConversionUtils {

    public List<UserListResponse> convertToUserListResponse(List<User> users) {
        return users.stream()
                .map(u -> new UserListResponse(
                        u.getId(),
                        u.getEmail(),
                        u.getRole(),
                        u.getFarmId()
                ))
                .collect(Collectors.toList());
    }
}