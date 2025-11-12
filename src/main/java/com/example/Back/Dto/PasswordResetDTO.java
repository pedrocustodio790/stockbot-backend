package com.example.Back.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // Adiciona Getters/Setters
public class PasswordResetDTO {

    @NotBlank(message = "A nova senha não pode estar em branco.")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    private String newPassword;
}