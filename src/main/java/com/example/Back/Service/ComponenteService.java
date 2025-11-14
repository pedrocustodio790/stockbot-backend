package com.example.Back.Service;

import com.example.Back.Dto.ComponenteDTO;
import com.example.Back.Entity.Componente;
import com.example.Back.Entity.Historico;
import com.example.Back.Entity.TipoMovimentacao;
import com.example.Back.Entity.Usuario; // 1. IMPORTAR USUARIO
import com.example.Back.Repository.ComponenteRepository;
import com.example.Back.Repository.HistoricoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ComponenteService {

    private final ComponenteRepository componenteRepository;
    private final HistoricoRepository historicoRepository;
    // 2. RequisicaoService removido (não era usado)

    public ComponenteService(ComponenteRepository componenteRepository, HistoricoRepository historicoRepository) {
        this.componenteRepository = componenteRepository;
        this.historicoRepository = historicoRepository;
    }

    // --- MÉTODOS PÚBLICOS DO SERVIÇO ---

    @Transactional(readOnly = true)
    public Page<ComponenteDTO> findAll(String termoDeBusca, Pageable pageable) {
        Page<Componente> componentesPage;

        if (termoDeBusca == null || termoDeBusca.trim().isEmpty()) {
            componentesPage = componenteRepository.findAll(pageable);
        } else {
            // (Assumindo que searchByTermo existe no seu Repositório)
            componentesPage = componenteRepository.searchByTermo(termoDeBusca, pageable);
        }

        // 3. MUDANÇA: Usamos o construtor do DTO (que tem a lógica do estoqueBaixo)
        // em vez do helper toDTO manual.
        return componentesPage.map(ComponenteDTO::new);
    }

    @Transactional
    public ComponenteDTO create(ComponenteDTO dto) {
        if (componenteRepository.existsByCodigoPatrimonio(dto.getCodigoPatrimonio())) {
            throw new IllegalArgumentException("Código de patrimônio já está em uso.");
        }

        Componente componente = toEntity(dto);
        Componente componenteSalvo = componenteRepository.save(componente);
        criarRegistroHistorico(componenteSalvo, TipoMovimentacao.ENTRADA, componenteSalvo.getQuantidade());

        // 3. MUDANÇA: Usamos o construtor do DTO
        return new ComponenteDTO(componenteSalvo);
    }

    @Transactional
    public ComponenteDTO update(Long id, ComponenteDTO dto) {
        Componente componenteExistente = componenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Componente não encontrado com o id: " + id));

        int quantidadeAntiga = componenteExistente.getQuantidade();

        // Atualiza a entidade (esta lógica está 100% correta)
        componenteExistente.setNome(dto.getNome());
        componenteExistente.setCodigoPatrimonio(dto.getCodigoPatrimonio());
        componenteExistente.setQuantidade(dto.getQuantidade());
        componenteExistente.setLocalizacao(dto.getLocalizacao());
        componenteExistente.setCategoria(dto.getCategoria());
        componenteExistente.setObservacoes(dto.getObservacoes());
        componenteExistente.setNivelMinimoEstoque(dto.getNivelMinimoEstoque());

        Componente componenteAtualizado = componenteRepository.save(componenteExistente);
        int quantidadeNova = componenteAtualizado.getQuantidade();
        int diferenca = quantidadeNova - quantidadeAntiga;

        if (diferenca != 0) {
            criarRegistroHistorico(componenteAtualizado, diferenca > 0 ? TipoMovimentacao.ENTRADA : TipoMovimentacao.SAIDA, Math.abs(diferenca));
        }

        // 3. MUDANÇA: Usamos o construtor do DTO
        return new ComponenteDTO(componenteAtualizado);
    }

    @Transactional
    public void delete(Long id) {
        if (!componenteRepository.existsById(id)) {
            throw new RuntimeException("Componente não encontrado com o id: " + id);
        }

        // (Sua lógica de apagar o histórico primeiro está correta)
        historicoRepository.deleteAllByComponenteId(id);
        componenteRepository.deleteById(id);
    }


    // --- MÉTODOS PRIVADOS (Helpers) ---

    // 4. MUDANÇA: Adicionado helper para pegar o usuário logado
    private Usuario getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        // Isso não deve acontecer se o SecurityFilter estiver correto
        throw new RuntimeException("Nenhum usuário autenticado encontrado.");
    }

    private void criarRegistroHistorico(Componente componente, TipoMovimentacao tipo, int quantidade) {
        // 5. MUDANÇA: Usamos o helper para pegar o email correto
        Usuario usuario = getAuthenticatedUser();

        Historico historico = new Historico();
        historico.setComponente(componente);
        historico.setTipo(tipo);
        historico.setQuantidade(quantidade);
        historico.setUsuario(usuario.getEmail()); // <-- Salva apenas o email
        historico.setDataHora(LocalDateTime.now());
        historico.setCodigoMovimentacao(UUID.randomUUID().toString());
        historicoRepository.save(historico);
    }

    // 6. MUDANÇA: O helper 'toDTO' foi removido
    // (Pois agora usamos o construtor ComponenteDTO::new)

    // O helper 'toEntity' está 100% correto
    private Componente toEntity(ComponenteDTO dto) {
        Componente componente = new Componente();
        componente.setNome(dto.getNome());
        componente.setCodigoPatrimonio(dto.getCodigoPatrimonio());
        componente.setQuantidade(dto.getQuantidade());
        componente.setLocalizacao(dto.getLocalizacao());
        componente.setCategoria(dto.getCategoria());
        componente.setObservacoes(dto.getObservacoes());
        componente.setNivelMinimoEstoque(dto.getNivelMinimoEstoque());
        return componente;
    }
}