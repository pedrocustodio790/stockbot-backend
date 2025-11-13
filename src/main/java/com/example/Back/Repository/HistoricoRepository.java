package com.example.Back.Repository;

import com.example.Back.Entity.Historico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 1. IMPORTAR
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {
    @Query(value = "SELECT h FROM Historico h LEFT JOIN FETCH h.componente", // 2. A Query
            countQuery = "SELECT COUNT(h) FROM Historico h") // 3. A Contagem
    Page<Historico> findAll(Pageable pageable);
    @Query("SELECT new com.example.Back.Dto.ItemMovimentadoDTO(h.componente.nome, COUNT(h)) " +
            "FROM Historico h " +
            "GROUP BY h.componente.nome " +
            "ORDER BY COUNT(h) DESC")
    Page<Historico> findByComponenteId(Long componenteId, Pageable pageable);
    void deleteAllByComponenteId(Long componenteId);
}