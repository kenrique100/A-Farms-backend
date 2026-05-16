package com.afarms.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;

@Data
public class SubUserRegisterRequest {

    @NotBlank @Email @Size(max = 100)
    private String email;

    @NotBlank @Size(min = 6, max = 100)
    private String password;

    @NotNull
    private UUID farmId;

    private String role = "SUB_USER";
}