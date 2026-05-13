package com.afarms.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SubUserRegisterRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private UUID farmId;

    private String role = "SUB_USER";
}