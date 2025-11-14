package com.example.Back.Repository;

import com.example.Back.Entity.PedidoCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Long> {

    // (Estes métodos estão OK por enquanto, mas NÃO filtram por domínio)
    @Query("SELECT p FROM PedidoCompra p " +
            "JOIN FETCH p.usuario u " +
            "LEFT JOIN FETCH p.aprovador a " +
            "WHERE p.status = :status")
    Page<PedidoCompra> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM PedidoCompra p " +
            "JOIN FETCH p.usuario u " +
            "LEFT JOIN FETCH p.aprovador a " +
            "WHERE u.id = :usuarioId")
    Page<PedidoCompra> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);


    // ✅ ADICIONE a nova contagem (filtrada por domínio)
    @Query("SELECT COUNT(p) FROM PedidoCompra p JOIN p.usuario u " +
            "WHERE p.status = :status AND u.dominio = :dominio")
    long countByStatusAndUsuarioDominio(@Param("status") String status, @Param("dominio") String dominio);
}