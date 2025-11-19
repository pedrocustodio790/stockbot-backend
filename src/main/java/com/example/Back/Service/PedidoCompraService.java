package com.example.Back.Service;

import com.example.Back.Dto.PedidoCompraCreateDTO;
import com.example.Back.Dto.PedidoCompraDTO;
import com.example.Back.Entity.PedidoCompra;
import com.example.Back.Entity.Usuario;
import com.example.Back.Repository.PedidoCompraRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class PedidoCompraService {

    private final PedidoCompraRepository pedidoCompraRepository;

    public PedidoCompraService(PedidoCompraRepository pedidoCompraRepository) {
        this.pedidoCompraRepository = pedidoCompraRepository;
    }

    // Helper de Segurança
    private Usuario getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        throw new RuntimeException("Nenhum usuário autenticado encontrado.");
    }

    @Transactional
    public PedidoCompra createPedido(PedidoCompraCreateDTO dto) {
        Usuario usuario = getAuthenticatedUser();

        PedidoCompra pedido = new PedidoCompra();
        pedido.setNomeItem(dto.getNomeItem());
        pedido.setQuantidade(dto.getQuantidade());
        pedido.setJustificativa(dto.getJustificativa());
        pedido.setUsuario(usuario);
        pedido.setStatus("PENDENTE");
        pedido.setDataPedido(new Date());

        return pedidoCompraRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public Page<PedidoCompraDTO> findMeusPedidos(Pageable pageable) {
        Usuario usuario = getAuthenticatedUser();
        return pedidoCompraRepository.findByUsuarioId(usuario.getId(), pageable)
                .map(PedidoCompraDTO::new);
    }

    // --- MUDANÇA CRÍTICA AQUI ---
    @Transactional(readOnly = true)
    public Page<PedidoCompraDTO> findPendentes(Pageable pageable) {
        // 1. Pega o admin logado
        Usuario admin = getAuthenticatedUser();

        // 2. Busca pendentes APENAS do domínio desse admin
        return pedidoCompraRepository.findByStatusAndDominio("PENDENTE", admin.getDominio(), pageable)
                .map(PedidoCompraDTO::new);
    }

    @Transactional
    public PedidoCompra aprovarPedido(Long id, String motivo, Usuario adminLogado) {
        // MUDANÇA: Busca já filtrando pelo domínio.
        // Se o ID for de outra empresa, retorna vazio e lança erro.
        PedidoCompra pedido = pedidoCompraRepository.findByIdAndDominio(id, adminLogado.getDominio())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado ou não pertence à sua empresa."));

        pedido.setStatus("APROVADO");
        pedido.setAprovador(adminLogado);
        pedido.setDataAcao(new Date());
        pedido.setMotivoAcao(motivo);

        return pedidoCompraRepository.save(pedido);
    }

    @Transactional
    public PedidoCompra recusarPedido(Long id, String motivo, Usuario adminLogado) {
        // MUDANÇA: Busca segura
        PedidoCompra pedido = pedidoCompraRepository.findByIdAndDominio(id, adminLogado.getDominio())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado ou não pertence à sua empresa."));

        pedido.setStatus("RECUSADO");
        pedido.setAprovador(adminLogado);
        pedido.setDataAcao(new Date());
        pedido.setMotivoAcao(motivo);

        return pedidoCompraRepository.save(pedido);
    }
}