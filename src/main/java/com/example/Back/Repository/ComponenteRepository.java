package com.example.Back.Repository;

import com.example.Back.Dto.CategoriaStatsDTO; // 1. IMPORTE O NOVO DTO
import com.example.Back.Dto.DashboardStatsDTO;
import com.example.Back.Entity.Componente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // (Importe se não tiver)
import org.springframework.stereotype.Repository;

import java.util.List; // 2. IMPORTE O java.util.List
import java.util.Optional;

@Repository
public interface ComponenteRepository extends JpaRepository<Componente, Long> {

    // (Seu método de busca paginada 'searchByTermo' já deve estar aqui)
    @Query(value = "SELECT c FROM Componente c WHERE " +
            "LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(c.codigoPatrimonio) LIKE LOWER(CONCAT('%', :termo, '%'))",
            countQuery = "SELECT COUNT(c) FROM Componente c WHERE " +
                    "LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
                    "LOWER(c.codigoPatrimonio) LIKE LOWER(CONCAT('%', :termo, '%'))")
    Page<Componente> searchByTermo(@Param("termo") String termo, Pageable pageable);

    // --- ✅ 3. ADICIONE A QUERY DO GRÁFICO DE CATEGORIA ---
    // (Soma a 'quantidade' de todos os itens, agrupados por 'categoria')
    @Query("SELECT new com.example.Back.Dto.CategoriaStatsDTO(c.categoria, SUM(c.quantidade)) " +
            "FROM Componente c " +
            "WHERE c.quantidade > 0 " + // (Não mostrar categorias com 0)
            "GROUP BY c.categoria " +
            "ORDER BY SUM(c.quantidade) DESC")
    List<CategoriaStatsDTO> getCategoriaStats();

    // --- ✅ 4. ADICIONE A QUERY DOS CARDS (ESTOQUE BAIXO) ---
    // (Conta quantos itens estão abaixo do seu 'nivelMinimoEstoque' individual)
    @Query("SELECT COUNT(c) FROM Componente c WHERE c.quantidade <= c.nivelMinimoEstoque")
    long countItensEstoqueBaixo();

    // --- ✅ 5. ADICIONE A QUERY DA LISTA (ESTOQUE BAIXO) ---
    // (Retorna a *lista* de itens que estão abaixo do nível mínimo)
    @Query("SELECT c FROM Componente c WHERE c.quantidade <= c.nivelMinimoEstoque")
    List<Componente> findItensEstoqueBaixo();


    boolean existsByCodigoPatrimonio(String codigoPatrimonio);
    Optional<Componente> findByCodigoPatrimonio(String codigoPatrimonio);
    long countByDominio(String dominio);

    // 2. Soma todas as UNIDADES (quantidade) do domínio
    @Query("SELECT COALESCE(SUM(c.quantidade), 0) FROM Componente c WHERE c.dominio = :dominio")
    long sumQuantidadeByDominio(String dominio);

    // 3. Conta itens zerados (Em Falta) no domínio
    @Query("SELECT COUNT(c) FROM Componente c WHERE c.dominio = :dominio AND c.quantidade = 0")
    long countItensEmFaltaByDominio(String dominio);

    // 4. Lista itens com estoque baixo (menor ou igual ao mínimo) no domínio
    @Query("SELECT c FROM Componente c WHERE c.dominio = :dominio AND c.quantidade <= c.nivelMinimoEstoque")
    List<Componente> findEstoqueBaixoByDominio(String dominio);

    // 5. Agrupa por Categoria para o Gráfico (Retorna o DTO direto!)
    @Query("SELECT new com.example.Back.Dto.DashboardStatsDTO(c.categoria, SUM(c.quantidade)) " +
            "FROM Componente c WHERE c.dominio = :dominio GROUP BY c.categoria")
    List<DashboardStatsDTO> countByCategoriaGrouped(String dominio);
}