package com.example.Back.Entity;

    import com.example.Back.Entity.Componente;
    import com.example.Back.Entity.Usuario;
    import jakarta.persistence.
            *;
    import lombok.Data;
    import java.time.LocalDateTime;
    import java.util.Date;

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

        @ManyToOne // O componente que foi pedido
        @JoinColumn(name = "componente_id", nullable = false)
        private Componente componente;

        // --- ✅ CAMPOS ADICIONADOS ---

        @Column(nullable = false)
        private Integer quantidade; // Quantos foram pedidos

        @Column(length = 1000) // Para justificativas
        private String observacao;

        @ManyToOne // O usuário que pediu
        @JoinColumn(name = "usuario_id", nullable = false)
        private Usuario usuario;

        @ManyToOne
        @JoinColumn(name = "aprovador_id") // É nulo até que alguém tome uma ação
        private Usuario aprovador;

        // 2. QUANDO foi a ação
        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "data_acao")
        private Date dataAcao;

        // 3. PORQUÊ (o motivo do admin)
        @Column(name = "motivo_acao", length = 1000)
        private String motivoAcao;

    }