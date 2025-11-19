package com.example.Back.Repository;

import com.example.Back.Dto.CategoriaStatsDTO;
import com.example.Back.Dto.DashboardStatsDTO;
import com.example.Back.Entity.Componente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponenteRepository extends JpaRepository<Componente, Long> {

    // --- 1. MÉTODOS CRUD PADRÃO (COM SEGURANÇA) ---

    // Busca Paginada SEGURA (Filtra pelo domínio)
    Page<Componente> findAllByDominio(String dominio, Pageable pageable);

    // Busca por Termo SEGURA (Filtra pelo domínio)
    @Query("SELECT c FROM Componente c WHERE " +
            "(c.dominio = :dominio) AND " + // <-- TRAVA DE SEGURANÇA
            "(LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(c.codigoPatrimonio) LIKE LOWER(CONCAT('%', :termo, '%')))")
    Page<Componente> searchByTermoAndDominio(@Param("termo") String termo,
                                             @Param("dominio") String dominio,
                                             Pageable pageable);

    // Validação de Duplicidade SEGURA (O código deve ser único APENAS dentro da empresa)
    boolean existsByCodigoPatrimonioAndDominio(String codigoPatrimonio, String dominio);

    // Busca unitária SEGURA
    Optional<Componente> findByIdAndDominio(Long id, String dominio);

    // Verifica existência para Delete
    boolean existsByIdAndDominio(Long id, String dominio);


    // --- 2. MÉTODOS PARA O DASHBOARD (KPIs & GRÁFICOS) ---

    // KPI 1: Contagem simples de itens (Spring Data gera automático pelo nome)
    long countByDominio(String dominio);

    // KPI 2: Soma total de unidades (Com tratamento para null)
    @Query("SELECT COALESCE(SUM(c.quantidade), 0) FROM Componente c WHERE c.dominio = :dominio")
    long sumQuantidadeByDominio(@Param("dominio") String dominio);

    // KPI 3: Contagem de itens zerados (Em Falta)
    @Query("SELECT COUNT(c) FROM Componente c WHERE c.dominio = :dominio AND c.quantidade = 0")
    long countItensEmFaltaByDominio(@Param("dominio") String dominio);

    // GRÁFICO: Agrupamento por Categoria
    // Retorna direto o DashboardStatsDTO para o Service do Dashboard
    @Query("SELECT new com.example.Back.Dto.DashboardStatsDTO(c.categoria, SUM(c.quantidade)) " +
            "FROM Componente c WHERE c.dominio = :dominio GROUP BY c.categoria")
    List<DashboardStatsDTO> countByCategoriaGrouped(@Param("dominio") String dominio);

    // LISTA: Itens com Estoque Baixo (Abaixo do mínimo)
    // (Removemos a duplicata e mantivemos apenas esta versão correta)
    @Query("SELECT c FROM Componente c WHERE c.dominio = :dominio AND c.quantidade <= c.nivelMinimoEstoque")
    List<Componente> findEstoqueBaixoByDominio(@Param("dominio") String dominio);

    // --- (Opcional) Método antigo de CategoriaStatsDTO ---
    // Se você não estiver usando este em outro lugar, pode apagar.
    // Mantive aqui caso tenha alguma lógica legada.
    @Query("SELECT new com.example.Back.Dto.CategoriaStatsDTO(c.categoria, SUM(c.quantidade)) " +
            "FROM Componente c " +
            "WHERE c.dominio = :dominio AND c.quantidade > 0 " +
            "GROUP BY c.categoria " +
            "ORDER BY SUM(c.quantidade) DESC")
    List<CategoriaStatsDTO> getCategoriaStatsByDominio(@Param("dominio") String dominio);
}