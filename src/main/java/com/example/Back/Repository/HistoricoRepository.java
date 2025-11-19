
package com.example.Back.Repository;

import com.example.Back.Entity.Historico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    // --- MUDANÇA CRÍTICA: O JOIN DE SEGURANÇA ---
    // Busca apenas históricos cujos componentes pertencem ao domínio do usuário
    @Query(value = "SELECT h FROM Historico h " +
            "JOIN FETCH h.componente c " + // Carrega o componente junto (Performance)
            "WHERE c.dominio = :dominio",  // <--- TRAVA DE SEGURANÇA
            countQuery = "SELECT COUNT(h) FROM Historico h " +
                    "JOIN h.componente c " +
                    "WHERE c.dominio = :dominio")
    Page<Historico> findAllByDominio(@Param("dominio") String dominio, Pageable pageable);

    // Busca histórico de um item específico (Seguro)
    @Query("SELECT h FROM Historico h " +
            "WHERE h.componente.id = :componenteId AND h.componente.dominio = :dominio")
    Page<Historico> findByComponenteIdAndDominio(@Param("componenteId") Long componenteId,
                                                 @Param("dominio") String dominio,
                                                 Pageable pageable);

    void deleteAllByComponenteId(Long componenteId);

    // OBS: Aquele método que retornava "ItemMovimentadoDTO" deve ficar
    // num Repository separado (ex: DashboardRepository) ou ser ajustado aqui
    // apenas se o tipo de retorno for DTO, não Entidade. Removi para compilar.
}