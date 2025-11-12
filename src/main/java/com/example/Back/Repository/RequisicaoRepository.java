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
            "WHERE r.status = :status",

            countQuery = "SELECT COUNT(r) FROM Requisicao r WHERE r.status = :status")
    Page<Requisicao> findByStatus(@Param("status") String status, Pageable pageable);
}