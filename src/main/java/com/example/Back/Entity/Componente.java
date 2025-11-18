package com.example.Back.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "componentes") // (O nome 'componentes' (plural) está correto)
public class Componente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String codigoPatrimonio;

    private int quantidade;
    private String localizacao;
    private String categoria;

    @Column(columnDefinition = "TEXT") // (Boa prática para observações longas)
    private String observacoes;

    // ✅ APAGAMOS A PRIMEIRA DECLARAÇÃO DUPLICADA DAQUI

    @OneToMany(mappedBy = "componente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Historico> historicos;

    @Column(nullable = false)
    private String dominio;

    // ✅ MANTEMOS APENAS ESTA DECLARAÇÃO (COM A ANOTAÇÃO)
    @Column(name = "nivel_minimo_estoque", nullable = false)
    private int nivelMinimoEstoque;

    // ✅ Este método está 100% CORRETO e é muito útil
    @Transient
    public boolean isEstoqueBaixo() {
        return this.quantidade <= this.nivelMinimoEstoque;
    }
}