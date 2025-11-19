package com.example.Back.Repository;

import com.example.Back.Entity.Requisicao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequisicaoRepository extends JpaRepository<Requisicao, Long> {

    // 1. Busca Pendentes (Segura por Domínio)
    @Query(value = "SELECT r FROM Requisicao r " +
            "JOIN FETCH r.usuario u " +
            "JOIN FETCH r.componente c " +
            "LEFT JOIN FETCH r.aprovador a " +
            "WHERE r.status = :status AND u.dominio = :dominio",
            countQuery = "SELECT COUNT(r) FROM Requisicao r " +
                    "JOIN r.usuario u " +
                    "WHERE r.status = :status AND u.dominio = :dominio")
    Page<Requisicao> findByStatusAndUsuarioDominio(
            @Param("status") String status,
            @Param("dominio") String dominio,
            Pageable pageable
    );

    // 2. Busca de Segurança para Aprovar/Recusar (NOVO)
    // Só retorna a requisição se ela pertencer ao domínio do admin
    @Query("SELECT r FROM Requisicao r " +
            "JOIN FETCH r.usuario u " +
            "JOIN FETCH r.componente c " +
            "WHERE r.id = :id AND u.dominio = :dominio")
    Optional<Requisicao> findByIdAndDominio(@Param("id") Long id,
                                            @Param("dominio") String dominio);

    // 3. KPI para Dashboard
    @Query("SELECT COUNT(r) FROM Requisicao r JOIN r.usuario u " +
            "WHERE r.status = :status AND u.dominio = :dominio")
    long countByStatusAndUsuarioDominio(@Param("status") String status, @Param("dominio") String dominio);
}