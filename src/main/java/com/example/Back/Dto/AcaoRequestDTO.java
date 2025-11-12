package com.example.Back.Dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AcaoRequestDTO {

    @NotBlank(message = "O motivo da ação é obrigatório.")
    private String motivo;
}