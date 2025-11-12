package com.example.Back.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date; // MUDANÇA: Import correto

// import java.time.LocalDateTime; // Não está sendo usado

@Entity
@Table(name = "requisicao")
@Data
public class Requisicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_requisicao")
    private Date dataRequisicao;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY) // O componente que foi pedido (otimizado)
    @JoinColumn(name = "componente_id", nullable = false)
    private Componente componente;

    @Column(nullable = false)
    private Integer quantidade; // Quantos foram pedidos

    @Column(length = 1000) // Para justificativas
    private String observacao;

    // --- CAMPO 1 (CORRETO) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false) // Dono do pedido
    private Usuario usuario;

    // --- CAMPO 2 (CORRIGIDO) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprovador_id", nullable = true) // MUDANÇA: Nome da coluna e nullable
    private Usuario aprovador; // Admin que aprovou/recusou

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_acao")
    private Date dataAcao;

    @Column(name = "motivo_acao", length = 1000)
    private String motivoAcao;
}