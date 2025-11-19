package com.example.Back.Repository;

import com.example.Back.Entity.PedidoCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Long> {

    // 1. Busca Pendentes SEGURA (Filtra por status E domínio do usuário dono do pedido)
    @Query("SELECT p FROM PedidoCompra p " +
            "JOIN FETCH p.usuario u " +         // Carrega usuário (performance)
            "LEFT JOIN FETCH p.aprovador a " +  // Carrega aprovador (se houver)
            "WHERE p.status = :status AND u.dominio = :dominio") // <--- TRAVA DE SEGURANÇA
    Page<PedidoCompra> findByStatusAndDominio(@Param("status") String status,
                                              @Param("dominio") String dominio,
                                              Pageable pageable);

    // 2. Meus Pedidos (Já filtra por ID do usuário, então é seguro, mas o JOIN ajuda na performance)
    @Query("SELECT p FROM PedidoCompra p " +
            "JOIN FETCH p.usuario u " +
            "LEFT JOIN FETCH p.aprovador a " +
            "WHERE u.id = :usuarioId")
    Page<PedidoCompra> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    // 3. Busca para Aprovar/Recusar (Garante que o pedido é do meu domínio)
    @Query("SELECT p FROM PedidoCompra p " +
            "JOIN FETCH p.usuario u " +
            "WHERE p.id = :id AND u.dominio = :dominio")
    Optional<PedidoCompra> findByIdAndDominio(@Param("id") Long id,
                                              @Param("dominio") String dominio);

    // 4. KPI para o Dashboard (Contagem segura)
    @Query("SELECT COUNT(p) FROM PedidoCompra p JOIN p.usuario u " +
            "WHERE p.status = :status AND u.dominio = :dominio")
    long countByStatusAndUsuarioDominio(@Param("status") String status,
                                        @Param("dominio") String dominio);
}