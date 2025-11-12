package com.example.Back.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequisicaoCreateDTO {

    @NotNull
    private Long componenteId;

    @NotNull
    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    private Integer quantidade;

    private String observacao; // Opcional (Justificativa)
}