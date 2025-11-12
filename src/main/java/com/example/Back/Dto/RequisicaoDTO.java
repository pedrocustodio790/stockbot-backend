package com.example.Back.Dto;

import com.example.Back.Entity.Requisicao;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class RequisicaoDTO {

    // Dados da própria requisição
    private Long id;
    private Integer quantidade;
    private String observacao;
    private String status;
    private Date dataRequisicao;

    // Dados do Usuário (para a coluna "Solicitante")
    private String usuarioNome;

    // Dados do Componente (para as colunas "Item" e "Patrimônio")
    private String componenteNome;
    private String componenteCodigoPatrimonio;

    // Construtor mágico: Transforma a Entidade (cheia) no DTO (limpo)
    public RequisicaoDTO(Requisicao requisicao) {
        this.id = requisicao.getId();
        this.quantidade = requisicao.getQuantidade();
        this.observacao = requisicao.getObservacao();
        this.status = requisicao.getStatus();
        this.dataRequisicao = requisicao.getDataRequisicao();

        // Pega os dados das entidades relacionadas
        this.usuarioNome = requisicao.getUsuario().getNome();
        this.componenteNome = requisicao.getComponente().getNome();
        this.componenteCodigoPatrimonio = requisicao.getComponente().getCodigoPatrimonio();
    }
}