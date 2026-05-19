package com.afarms.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Email
    @Size(max = 100)
    private String email;

    @Size(min = 2, max = 50)
    private String username;

    private String role;
}