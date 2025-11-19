package com.example.Back.Service;

import com.example.Back.Dto.RequisicaoCreateDTO;
import com.example.Back.Dto.RequisicaoDTO;
import com.example.Back.Entity.Componente;
import com.example.Back.Entity.Historico;
import com.example.Back.Entity.Requisicao;
import com.example.Back.Entity.TipoMovimentacao;
import com.example.Back.Entity.Usuario;
import com.example.Back.Repository.ComponenteRepository;
import com.example.Back.Repository.HistoricoRepository;
import com.example.Back.Repository.RequisicaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
public class RequisicaoService {

    private final RequisicaoRepository requisicaoRepository;
    private final ComponenteRepository componenteRepository;
    private final HistoricoRepository historicoRepository;

    public RequisicaoService(RequisicaoRepository requisicaoRepository,
                             ComponenteRepository componenteRepository,
                             HistoricoRepository historicoRepository) {
        this.requisicaoRepository = requisicaoRepository;
        this.componenteRepository = componenteRepository;
        this.historicoRepository = historicoRepository;
    }

    // --- HELPER DE SEGURANÇA ---
    private Usuario getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        throw new RuntimeException("Sessão inválida ou expirada.");
    }

    @Transactional
    public Requisicao createRequisicao(RequisicaoCreateDTO dto) {
        Usuario usuario = getAuthenticatedUser();

        // Busca o componente e verifica se pertence ao domínio do usuário
        Componente componente = componenteRepository.findByIdAndDominio(dto.getComponenteId(), usuario.getDominio())
                .orElseThrow(() -> new RuntimeException("Componente não encontrado ou não pertence à sua empresa."));

        if (dto.getQuantidade() > componente.getQuantidade()) {
            throw new IllegalArgumentException("Quantidade indisponível em estoque. Atual: " + componente.getQuantidade());
        }

        Requisicao req = new Requisicao();
        req.setUsuario(usuario);
        req.setComponente(componente);
        req.setQuantidade(dto.getQuantidade());
        req.setObservacao(dto.getObservacao());
        req.setStatus("PENDENTE");
        req.setDataRequisicao(new Date());

        return requisicaoRepository.save(req);
    }

    @Transactional(readOnly = true)
    public Page<RequisicaoDTO> findPendentes(Pageable pageable) {
        Usuario admin = getAuthenticatedUser();
        return requisicaoRepository.findByStatusAndUsuarioDominio("PENDENTE", admin.getDominio(), pageable)
                .map(RequisicaoDTO::new);
    }

    @Transactional
    public RequisicaoDTO aprovarRequisicao(Long id, String motivo) {
        Usuario adminLogado = getAuthenticatedUser();

        // 1. Busca SEGURA (Já filtra pelo domínio)
        Requisicao requisicao = requisicaoRepository.findByIdAndDominio(id, adminLogado.getDominio())
                .orElseThrow(() -> new RuntimeException("Requisição não encontrada ou acesso negado."));

        if (!"PENDENTE".equals(requisicao.getStatus())) {
            throw new RuntimeException("Esta requisição já foi processada.");
        }

        Componente componente = requisicao.getComponente();
        int quantidadePedida = requisicao.getQuantidade();

        // 2. Verifica Estoque Novamente (Pode ter mudado desde o pedido)
        if (quantidadePedida > componente.getQuantidade()) {
            throw new RuntimeException("Estoque insuficiente para aprovar agora. Restam: " + componente.getQuantidade());
        }

        // 3. Atualiza Estoque
        componente.setQuantidade(componente.getQuantidade() - quantidadePedida);
        componenteRepository.save(componente);

        // 4. Gera Histórico
        criarRegistroHistorico(componente, TipoMovimentacao.SAIDA, quantidadePedida, adminLogado.getEmail());

        // 5. Finaliza Requisição
        requisicao.setStatus("APROVADO");
        requisicao.setAprovador(adminLogado);
        requisicao.setDataAcao(new Date());
        requisicao.setMotivoAcao(motivo);

        return new RequisicaoDTO(requisicaoRepository.save(requisicao));
    }

    @Transactional
    public RequisicaoDTO recusarRequisicao(Long id, String motivo) {
        Usuario adminLogado = getAuthenticatedUser();

        Requisicao requisicao = requisicaoRepository.findByIdAndDominio(id, adminLogado.getDominio())
                .orElseThrow(() -> new RuntimeException("Requisição não encontrada ou acesso negado."));

        if (!"PENDENTE".equals(requisicao.getStatus())) {
            throw new RuntimeException("Esta requisição já foi processada.");
        }

        requisicao.setStatus("RECUSADO");
        requisicao.setAprovador(adminLogado);
        requisicao.setDataAcao(new Date());
        requisicao.setMotivoAcao(motivo);

        return new RequisicaoDTO(requisicaoRepository.save(requisicao));
    }

    private void criarRegistroHistorico(Componente componente, TipoMovimentacao tipo, int quantidade, String usuarioEmail) {
        Historico historico = new Historico();
        historico.setComponente(componente);
        historico.setTipo(tipo);
        historico.setQuantidade(quantidade);
        historico.setUsuario(usuarioEmail);
        historico.setDataHora(LocalDateTime.now());
        historico.setCodigoMovimentacao(UUID.randomUUID().toString());
        historicoRepository.save(historico);
    }
}