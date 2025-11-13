package com.example.Back.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Usando um 'record' limpo para o login
public record LoginDTO(
        @NotBlank @Email
        String email,

        @NotBlank
        String senha,

        @NotBlank
        String dominio
) {}