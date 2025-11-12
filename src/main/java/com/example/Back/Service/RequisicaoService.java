package com.example.Back.Service;

// ✅ Imports de DTOs (VAMOS CRIAR O RequisicaoDTO)
import com.example.Back.Dto.RequisicaoCreateDTO;
import com.example.Back.Dto.RequisicaoDTO;
// (Você também vai precisar do AcaoRequestDTO)

import com.example.Back.Entity.Componente;
import com.example.Back.Entity.Requisicao;
import com.example.Back.Entity.Usuario;
import com.example.Back.Repository.ComponenteRepository;
import com.example.Back.Repository.RequisicaoRepository;
import com.example.Back.Repository.UsuarioRepository;

// ✅ IMPORTS CORRIGIDOS E ADICIONADOS
import org.springframework.transaction.annotation.Transactional; // <-- O CORRETO
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RequisicaoService {

    private final RequisicaoRepository requisicaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComponenteRepository componenteRepository;

    public RequisicaoService(RequisicaoRepository requisicaoRepository,
                             UsuarioRepository usuarioRepository,
                             ComponenteRepository componenteRepository) {
        this.requisicaoRepository = requisicaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.componenteRepository = componenteRepository;
    }

    // --- Método que você já tinha ---
    @Transactional
    public Requisicao createRequisicao(RequisicaoCreateDTO dto) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        Componente componente = componenteRepository.findById(dto.getComponenteId())
                .orElseThrow(() -> new RuntimeException("Componente não encontrado"));

        Requisicao req = new Requisicao();
        req.setUsuario(usuario);
        req.setComponente(componente);
        req.setQuantidade(dto.getQuantidade());
        req.setObservacao(dto.getObservacao());
        req.setStatus("PENDENTE");
        req.setDataRequisicao(new Date());

        return requisicaoRepository.save(req);
    }

    // ======================================================
    // ✅ MÉTODOS FALTANTES (Para o Admin)
    // ======================================================

    /**
     * Busca uma página de requisições PENDENTES.
     * (Usado pelo RequisicaoController)
     */
    @Transactional(readOnly = true)
    public Page<RequisicaoDTO> findPendentes(Pageable pageable) {
        // Busca as Entidades do banco
        Page<Requisicao> paginaDeEntidades = requisicaoRepository.findByStatus("PENDENTE", pageable);

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

        requisicao.setStatus("APROVADO");

        // Preenche os campos de AUDITORIA
        requisicao.setAprovador(adminLogado);
        requisicao.setDataAcao(new Date());
        requisicao.setMotivoAcao(motivo);

        Requisicao requisicaoSalva = requisicaoRepository.save(requisicao);

        // (Aqui você também precisaria diminuir a quantidade do Componente no estoque)
        // ... (lógica de estoque) ...

        return new RequisicaoDTO(requisicaoSalva);
    }

    /**
     * Recusa uma requisição e salva a auditoria.
     */
    @Transactional
    public RequisicaoDTO recusarRequisicao(Long id, String motivo, Usuario adminLogado) {
        Requisicao requisicao = requisicaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisição não encontrada"));

        requisicao.setStatus("RECUSADO");

        // Preenche os campos de AUDITORIA
        requisicao.setAprovador(adminLogado);
        requisicao.setDataAcao(new Date());
        requisicao.setMotivoAcao(motivo);

        Requisicao requisicaoSalva = requisicaoRepository.save(requisicao);
        return new RequisicaoDTO(requisicaoSalva);
    }
}