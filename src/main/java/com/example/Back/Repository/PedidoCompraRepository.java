package com.example.Back.Repository;

import com.example.Back.Entity.PedidoCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 1. Importar @Query
import org.springframework.data.repository.query.Param; // 2. Importar @Param

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Long> {

    @Query("SELECT p FROM PedidoCompra p " +
            "JOIN FETCH p.usuario u " +
            "LEFT JOIN FETCH p.aprovador a " + // Usamos LEFT JOIN pois aprovador pode ser nulo
            "WHERE p.status = :status")
    Page<PedidoCompra> findByStatus(@Param("status") String status, Pageable pageable);


    @Query("SELECT p FROM PedidoCompra p " +
            "JOIN FETCH p.usuario u " +
            "LEFT JOIN FETCH p.aprovador a " +
            "WHERE u.id = :usuarioId")
    Page<PedidoCompra> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);
}