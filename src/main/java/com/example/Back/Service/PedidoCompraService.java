package com.example.Back.Service;

import com.example.Back.Dto.PedidoCompraCreateDTO;
import com.example.Back.Dto.PedidoCompraDTO;
import com.example.Back.Entity.PedidoCompra;
import com.example.Back.Entity.Usuario;
import com.example.Back.Repository.PedidoCompraRepository;
// MUDANÇA: Não precisamos mais do UsuarioRepository aqui
// import com.example.Back.Repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class PedidoCompraService {

    private final PedidoCompraRepository pedidoCompraRepository;
    // MUDANÇA: Não precisamos mais do UsuarioRepository
    // private final UsuarioRepository usuarioRepository;

    // MUDANÇA: O construtor não precisa mais do UsuarioRepository
    public PedidoCompraService(PedidoCompraRepository pedidoCompraRepository) {
        this.pedidoCompraRepository = pedidoCompraRepository;
    }

    // --- MUDANÇA: NOVO MÉTODO HELPER ---
    // Pega o usuário (com domínio) que está logado no momento
    private Usuario getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        // Isso não deve acontecer se o SecurityFilter estiver correto
        throw new RuntimeException("Nenhum usuário autenticado encontrado.");
    }


    // Para o Usuário criar um pedido (MÉTODO CORRIGIDO)
    @Transactional
    public PedidoCompra createPedido(PedidoCompraCreateDTO dto) {
        // MUDANÇA: Usamos o helper para pegar o usuário
        Usuario usuario = getAuthenticatedUser();
        // (As linhas antigas que usavam 'findByEmail' foram removidas)

        PedidoCompra pedido = new PedidoCompra();
        pedido.setNomeItem(dto.getNomeItem());
        pedido.setQuantidade(dto.getQuantidade());
        pedido.setJustificativa(dto.getJustificativa());
        pedido.setUsuario(usuario); // Seta o usuário autenticado
        pedido.setStatus("PENDENTE");
        pedido.setDataPedido(new Date());

        return pedidoCompraRepository.save(pedido);
    }

    // Para o Usuário ver seus pedidos (MÉTODO CORRIGIDO E OTIMIZADO)
    @Transactional(readOnly = true)
    public Page<PedidoCompraDTO> findMeusPedidos(Pageable pageable) {
        // MUDANÇA: Usamos o helper para pegar o usuário
        Usuario usuario = getAuthenticatedUser();
        // (As linhas antigas que usavam 'findByEmail' foram removidas)

        // A lógica de paginação chama o repositório pelo ID do usuário
        return pedidoCompraRepository.findByUsuarioId(usuario.getId(), pageable)
                .map(PedidoCompraDTO::new);
    }

    // Para o Admin ver os pendentes (Este método está perfeito)
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

        // Garante que um admin só aprove pedidos do seu próprio domínio
        if (!pedido.getUsuario().getDominio().equals(adminLogado.getDominio())) {
            throw new RuntimeException("Acesso negado. O pedido não pertence a este domínio.");
        }

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

        // Garante que um admin só recuse pedidos do seu próprio domínio
        if (!pedido.getUsuario().getDominio().equals(adminLogado.getDominio())) {
            throw new RuntimeException("Acesso negado. O pedido não pertence a este domínio.");
        }

        pedido.setStatus("RECUSADO");
        pedido.setAprovador(adminLogado);
        pedido.setDataAcao(new Date());
        pedido.setMotivoAcao(motivo);

        return pedidoCompraRepository.save(pedido);
    }
}