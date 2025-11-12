package com.example.Back.Service;

import com.example.Back.Dto.PedidoCompraCreateDTO;
import com.example.Back.Dto.PedidoCompraDTO;
import com.example.Back.Entity.PedidoCompra;
import com.example.Back.Entity.Usuario;
import com.example.Back.Repository.PedidoCompraRepository;
import com.example.Back.Repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
// MUDANÇA: Não precisamos mais de List ou Collectors aqui
// import java.util.List;
// import java.util.stream.Collectors;

@Service
public class PedidoCompraService {

    private final PedidoCompraRepository pedidoCompraRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoCompraService(PedidoCompraRepository pedidoCompraRepository, UsuarioRepository usuarioRepository) {
        this.pedidoCompraRepository = pedidoCompraRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Para o Usuário criar um pedido (Este método está perfeito)
    @Transactional
    public PedidoCompra createPedido(PedidoCompraCreateDTO dto) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PedidoCompra pedido = new PedidoCompra();
        pedido.setNomeItem(dto.getNomeItem());
        pedido.setQuantidade(dto.getQuantidade());
        pedido.setJustificativa(dto.getJustificativa());
        pedido.setUsuario(usuario);
        pedido.setStatus("PENDENTE");
        pedido.setDataPedido(new Date());

        return pedidoCompraRepository.save(pedido);
    }

    // Para o Usuário ver seus pedidos (MÉTODO OTIMIZADO)
    @Transactional(readOnly = true)
    // MUDANÇA: O método agora recebe Pageable...
    public Page<PedidoCompraDTO> findMeusPedidos(Pageable pageable) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // MUDANÇA: ...chama o repositório paginado...
        return pedidoCompraRepository.findByUsuarioId(usuario.getId(), pageable)
                .map(PedidoCompraDTO::new); // ...e retorna uma Page<DTO>
    }

    // Para o Admin ver os pendentes (Este método já estava perfeito)
    @Transactional(readOnly = true)
    public Page<PedidoCompraDTO> findPendentes(Pageable pageable) {
        return pedidoCompraRepository.findByStatus("PENDENTE", pageable)
                .map(PedidoCompraDTO::new);
    }

    // Para o Admin aprovar (Este método está perfeito)
    @Transactional
    public PedidoCompra aprovarPedido(Long id, String motivo, Usuario adminLogado) {
        PedidoCompra pedido = pedidoCompraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setStatus("APROVADO");
        pedido.setAprovador(adminLogado);
        pedido.setDataAcao(new Date());
        pedido.setMotivoAcao(motivo);

        return pedidoCompraRepository.save(pedido);
    }

    // Para o Admin recusar (Este método está perfeito)
    @Transactional
    public PedidoCompra recusarPedido(Long id, String motivo, Usuario adminLogado) {
        PedidoCompra pedido = pedidoCompraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setStatus("RECUSADO");
        pedido.setAprovador(adminLogado);
        pedido.setDataAcao(new Date());
        pedido.setMotivoAcao(motivo);

        return pedidoCompraRepository.save(pedido);
    }
}