package com.example.Back.Repository;

import com.example.Back.Entity.Componente;
import org.springframework.data.domain.Page; // 1. Importar Page
import org.springframework.data.domain.Pageable; // 2. Importar Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComponenteRepository extends JpaRepository<Componente, Long> {

    // Esses métodos estão ótimos, não precisam mudar
    boolean existsByCodigoPatrimonio(String codigoPatrimonio);
    Optional<Componente> findByCodigoPatrimonio(String codigoPatrimonio);


    @Query(value = "SELECT c FROM Componente c WHERE " +
            "LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(c.codigoPatrimonio) LIKE LOWER(CONCAT('%', :termo, '%'))",

            // 4. Query de contagem (para a paginação saber o total)
            countQuery = "SELECT COUNT(c) FROM Componente c WHERE " +
                    "LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
                    "LOWER(c.codigoPatrimonio) LIKE LOWER(CONCAT('%', :termo, '%'))")
    Page<Componente> searchByTermo(@Param("termo") String termo, Pageable pageable);

    // NOTA: O JpaRepository JÁ nos dá o 'findAll(Pageable pageable)' de graça.
    // Não precisamos declarar ele.
}