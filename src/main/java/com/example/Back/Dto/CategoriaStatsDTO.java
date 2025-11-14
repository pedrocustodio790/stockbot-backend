package com.example.Back.Dto;

public record CategoriaStatsDTO(
        String categoria,
        long quantidadeTotal // (Total de itens somados daquela categoria)
) {}