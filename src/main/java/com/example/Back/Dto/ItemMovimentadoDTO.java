package com.example.Back.Dto;

import lombok.Data;

@Data
public class ItemMovimentadoDTO {

    // ✅ Erro 1: Corrigido para "camelCase" (minúscula)
    private String nomeComponente;

    // ✅ Erro 2: Corrigido para "camelCase" e sem acento
    private Long totalMovimentacoes;

    public ItemMovimentadoDTO(String nomeComponente, Long totalMovimentacoes) {
        // Agora o construtor encontra os campos corretos
        this.nomeComponente = nomeComponente;
        this.totalMovimentacoes = totalMovimentacoes;
    }

    // Getters (que agora funcionam)
    public String getNomeComponente() {
        return nomeComponente;
    }

    public Long getTotalMovimentacoes() {
        return totalMovimentacoes;
    }
}
