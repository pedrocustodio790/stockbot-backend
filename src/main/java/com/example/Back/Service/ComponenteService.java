package com.example.Back.Service;

import com.example.Back.Dto.ComponenteDTO;
import com.example.Back.Entity.Componente;
import com.example.Back.Entity.Historico;
import com.example.Back.Entity.TipoMovimentacao;
import com.example.Back.Entity.Usuario;
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

    public ComponenteService(ComponenteRepository componenteRepository, HistoricoRepository historicoRepository) {
        this.componenteRepository = componenteRepository;
        this.historicoRepository = historicoRepository;
    }

    private Usuario getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        throw new RuntimeException("Erro de segurança: Usuário não autenticado.");
    }


    @Transactional(readOnly = true)
    public Page<ComponenteDTO> findAll(String termoDeBusca, Pageable pageable) {
        // Pega o domínio do usuário logado
        String userDominio = getAuthenticatedUser().getDominio();
        Page<Componente> page;

        if (termoDeBusca == null || termoDeBusca.trim().isEmpty()) {
            // Busca tudo DO DOMÍNIO
            page = componenteRepository.findAllByDominio(userDominio, pageable);
        } else {
            // Busca por termo DENTRO DO DOMÍNIO
            page = componenteRepository.searchByTermoAndDominio(termoDeBusca, userDominio, pageable);
        }

        return page.map(ComponenteDTO::new);
    }


    @Transactional
    public ComponenteDTO create(ComponenteDTO dto) {
        Usuario usuario = getAuthenticatedUser();
        String userDominio = usuario.getDominio();

        // Verifica duplicidade APENAS dentro desse domínio
        if (componenteRepository.existsByCodigoPatrimonioAndDominio(dto.getCodigoPatrimonio(), userDominio)) {
            throw new IllegalArgumentException("O código '" + dto.getCodigoPatrimonio() + "' já existe no seu estoque.");
        }

        Componente componente = toEntity(dto);

        componente.setDominio(userDominio);

        Componente salvo = componenteRepository.save(componente);

        // Registra histórico
        criarRegistroHistorico(salvo, TipoMovimentacao.ENTRADA, salvo.getQuantidade(), usuario);

        return new ComponenteDTO(salvo);
    }

    // --- 3. ATUALIZAÇÃO (Verifica posse) ---
    @Transactional
    public ComponenteDTO update(Long id, ComponenteDTO dto) {
        Usuario usuario = getAuthenticatedUser();

        // Só encontra se o ID for válido E pertencer ao domínio do usuário
        Componente componente = componenteRepository.findByIdAndDominio(id, usuario.getDominio())
                .orElseThrow(() -> new RuntimeException("Item não encontrado ou você não tem permissão para editá-lo."));

        int qtdAntiga = componente.getQuantidade();

        // Atualiza dados
        componente.setNome(dto.getNome());
        componente.setCodigoPatrimonio(dto.getCodigoPatrimonio()); // Opcional: validar duplicidade aqui também se mudar o código
        componente.setQuantidade(dto.getQuantidade());
        componente.setLocalizacao(dto.getLocalizacao());
        componente.setCategoria(dto.getCategoria());
        componente.setObservacoes(dto.getObservacoes());
        componente.setNivelMinimoEstoque(dto.getNivelMinimoEstoque());

        Componente atualizado = componenteRepository.save(componente);

        // Lógica de histórico
        int diferenca = atualizado.getQuantidade() - qtdAntiga;
        if (diferenca != 0) {
            TipoMovimentacao tipo = diferenca > 0 ? TipoMovimentacao.ENTRADA : TipoMovimentacao.SAIDA;
            criarRegistroHistorico(atualizado, tipo, Math.abs(diferenca), usuario);
        }

        return new ComponenteDTO(atualizado);
    }

    // --- 4. DELEÇÃO (Verifica posse) ---
    @Transactional
    public void delete(Long id) {
        String userDominio = getAuthenticatedUser().getDominio();

        // Só deleta se existir E for do meu domínio
        if (!componenteRepository.existsByIdAndDominio(id, userDominio)) {
            throw new RuntimeException("Item não encontrado.");
        }

        historicoRepository.deleteAllByComponenteId(id);
        componenteRepository.deleteById(id);
    }

    // --- Helpers ---

    private void criarRegistroHistorico(Componente comp, TipoMovimentacao tipo, int qtd, Usuario user) {
        Historico h = new Historico();
        h.setComponente(comp);
        h.setTipo(tipo);
        h.setQuantidade(qtd);
        h.setUsuario(user.getEmail()); // Email de quem fez a ação
        h.setDataHora(LocalDateTime.now());
        h.setCodigoMovimentacao(UUID.randomUUID().toString());
        historicoRepository.save(h);
    }

    private Componente toEntity(ComponenteDTO dto) {
        Componente c = new Componente();
        c.setNome(dto.getNome());
        c.setCodigoPatrimonio(dto.getCodigoPatrimonio());
        c.setQuantidade(dto.getQuantidade());
        c.setLocalizacao(dto.getLocalizacao());
        c.setCategoria(dto.getCategoria());
        c.setObservacoes(dto.getObservacoes());
        c.setNivelMinimoEstoque(dto.getNivelMinimoEstoque());
        // OBS: O domínio é setado no método create, não aqui
        return c;
    }
}