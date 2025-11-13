package com.example.Back.Service;

import com.example.Back.Dto.RequisicaoCreateDTO;
import com.example.Back.Dto.RequisicaoDTO;
import com.example.Back.Entity.Componente;
import com.example.Back.Entity.Historico; // MUDANÇA: Importar Historico
import com.example.Back.Entity.Requisicao;
import com.example.Back.Entity.TipoMovimentacao; // MUDANÇA: Importar TipoMovimentacao
import com.example.Back.Entity.Usuario;
import com.example.Back.Repository.ComponenteRepository;
import com.example.Back.Repository.HistoricoRepository; // MUDANÇA: Importar HistoricoRepository
import com.example.Back.Repository.RequisicaoRepository;
// MUDANÇA: Não precisamos mais do UsuarioRepository
// import com.example.Back.Repository.UsuarioRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
// MUDANÇA: Não precisamos mais do UsernameNotFoundException
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // MUDANÇA: Importar LocalDateTime
import java.util.Date;
import java.util.UUID; // MUDANÇA: Importar UUID

@Service
public class RequisicaoService {

    private final RequisicaoRepository requisicaoRepository;
    private final ComponenteRepository componenteRepository;
    // MUDANÇA: Adicionar o HistoricoRepository para criar o log
    private final HistoricoRepository historicoRepository;

    // MUDANÇA: Atualizar o construtor
    public RequisicaoService(RequisicaoRepository requisicaoRepository,
                             ComponenteRepository componenteRepository,
                             HistoricoRepository historicoRepository) {
        this.requisicaoRepository = requisicaoRepository;
        this.componenteRepository = componenteRepository;
        this.historicoRepository = historicoRepository;
    }

    // --- MUDANÇA: NOVO MÉTODO HELPER ---
    private Usuario getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        throw new RuntimeException("Nenhum usuário autenticado encontrado.");
    }

    // --- Método que você já tinha (MÉTODO CORRIGIDO) ---
    @Transactional
    public Requisicao createRequisicao(RequisicaoCreateDTO dto) {
        // MUDANÇA: Usamos o helper para pegar o usuário
        Usuario usuario = getAuthenticatedUser();

        Componente componente = componenteRepository.findById(dto.getComponenteId())
                .orElseThrow(() -> new RuntimeException("Componente não encontrado"));

        // MUDANÇA: Validação de estoque (não deixar pedir mais do que tem)
        if (dto.getQuantidade() > componente.getQuantidade()) {
            throw new RuntimeException("Quantidade solicitada (" + dto.getQuantidade()
                    + ") é maior que o estoque (" + componente.getQuantidade() + ").");
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
        // MUDANÇA: Verifica o domínio do Admin logado
        Usuario admin = getAuthenticatedUser();

        // MUDANÇA: Busca apenas requisições do domínio do admin
        Page<Requisicao> paginaDeEntidades = requisicaoRepository.findByStatusAndUsuarioDominio("PENDENTE", admin.getDominio(), pageable);

        // Converte (mapeia) para DTOs
        return paginaDeEntidades.map(RequisicaoDTO::new);
    }

    /**
     * Aprova uma requisição e salva a auditoria.
     */
    @Transactional
    public RequisicaoDTO aprovarRequisicao(Long id, String motivo, Usuario adminLogado) {
        Requisicao requisicao = requisicaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisição não encontrada"));

        // MUDANÇA: Checagem de segurança de domínio
        if (!requisicao.getUsuario().getDominio().equals(adminLogado.getDominio())) {
            throw new RuntimeException("Acesso negado. A requisição não pertence a este domínio.");
        }

        // MUDANÇA: LÓGICA DE ESTOQUE (O BUG BÔNUS)
        Componente componente = requisicao.getComponente();
        int quantidadePedida = requisicao.getQuantidade();

        if (quantidadePedida > componente.getQuantidade()) {
            throw new RuntimeException("Não foi possível aprovar. Estoque atual (" + componente.getQuantidade() + ") é menor que o solicitado.");
        }

        // 1. Reduz o estoque do componente
        componente.setQuantidade(componente.getQuantidade() - quantidadePedida);
        componenteRepository.save(componente);

        // 2. Cria o registro no Histórico
        criarRegistroHistorico(componente, TipoMovimentacao.SAIDA, quantidadePedida, adminLogado.getEmail(), "Aprovado: " + motivo);

        // 3. Atualiza a requisição
        requisicao.setStatus("APROVADO");
        requisicao.setAprovador(adminLogado);
        requisicao.setDataAcao(new Date());
        requisicao.setMotivoAcao(motivo);
        Requisicao requisicaoSalva = requisicaoRepository.save(requisicao);

        return new RequisicaoDTO(requisicaoSalva);
    }

    /**
     * Recusa uma requisição e salva a auditoria.
     */
    @Transactional
    public RequisicaoDTO recusarRequisicao(Long id, String motivo, Usuario adminLogado) {
        Requisicao requisicao = requisicaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisição não encontrada"));

        // MUDANÇA: Checagem de segurança de domínio
        if (!requisicao.getUsuario().getDominio().equals(adminLogado.getDominio())) {
            throw new RuntimeException("Acesso negado. A requisição não pertence a este domínio.");
        }

        requisicao.setStatus("RECUSADO");
        requisicao.setAprovador(adminLogado);
        requisicao.setDataAcao(new Date());
        requisicao.setMotivoAcao(motivo);
        Requisicao requisicaoSalva = requisicaoRepository.save(requisicao);

        return new RequisicaoDTO(requisicaoSalva);
    }

    // MUDANÇA: Método helper para criar o log no histórico
    private void criarRegistroHistorico(Componente componente, TipoMovimentacao tipo, int quantidade, String emailUsuario, String observacao) {
        Historico historico = new Historico();
        historico.setComponente(componente);
        historico.setTipo(tipo);
        historico.setQuantidade(quantidade);
        historico.setUsuario(emailUsuario); // O Admin que aprovou
        historico.setDataHora(LocalDateTime.now());
        historico.setCodigoMovimentacao(UUID.randomUUID().toString());
        // historico.setObservacao(observacao); // (Se você adicionar 'observacao' na entidade Historico)
        historicoRepository.save(historico);
    }
}