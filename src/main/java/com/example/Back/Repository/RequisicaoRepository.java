package com.example.Back.Repository;

import com.example.Back.Entity.Requisicao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 1. IMPORTAR
import org.springframework.data.repository.query.Param; // 2. IMPORTAR
import org.springframework.stereotype.Repository;

@Repository
public interface RequisicaoRepository extends JpaRepository<Requisicao, Long> {

    @Query(value = "SELECT r FROM Requisicao r " +
            "JOIN FETCH r.usuario u " +
            "JOIN FETCH r.componente c " +
            "LEFT JOIN FETCH r.aprovador a " +
            "WHERE r.status = :status AND u.dominio = :dominio", // 1. Adicionamos o filtro de domínio

            countQuery = "SELECT COUNT(r) FROM Requisicao r " +
                    "JOIN r.usuario u " + // 2. O Count tbm precisa do JOIN
                    "WHERE r.status = :status AND u.dominio = :dominio") // 3. E do filtro
    Page<Requisicao> findByStatusAndUsuarioDominio(
            @Param("status") String status,
            @Param("dominio") String dominio,
            Pageable pageable
    );

}
