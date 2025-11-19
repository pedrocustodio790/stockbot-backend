package com.example.Back.Service;

import com.example.Back.Dto.HistoricoDTO;
import com.example.Back.Entity.Historico;
import com.example.Back.Entity.Usuario; // Importar
import com.example.Back.Repository.HistoricoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder; // Importar
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoricoService {

    private final HistoricoRepository historicoRepository;

    public HistoricoService(HistoricoRepository historicoRepository) {
        this.historicoRepository = historicoRepository;
    }

    // --- HELPER DE SEGURANÇA ---
    private Usuario getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        throw new RuntimeException("Usuário não autenticado.");
    }

    @Transactional(readOnly = true)
    public Page<HistoricoDTO> findAllPaginated(Pageable pageable) {
        // 1. Pega o domínio do usuário logado
        String userDominio = getAuthenticatedUser().getDominio();

        // 2. Busca APENAS o histórico dessa empresa
        Page<Historico> historicoPage = historicoRepository.findAllByDominio(userDominio, pageable);

        return historicoPage.map(this::toDTO);
    }

    // Se você precisar filtrar o histórico de um único item no futuro:
    @Transactional(readOnly = true)
    public Page<HistoricoDTO> findByComponente(Long componenteId, Pageable pageable) {
        String userDominio = getAuthenticatedUser().getDominio();

        return historicoRepository.findByComponenteIdAndDominio(componenteId, userDominio, pageable)
                .map(this::toDTO);
    }

    private HistoricoDTO toDTO(Historico historico) {
        // Tratamento de Null Safety caso o componente tenha sido deletado logicamente
        // (embora seu cascade seja delete físico, é bom prevenir)
        String componenteNome = (historico.getComponente() != null)
                ? historico.getComponente().getNome()
                : "Item Removido";

        Long componenteId = (historico.getComponente() != null)
                ? historico.getComponente().getId()
                : null;

        return new HistoricoDTO(
                historico.getId(),
                componenteId,
                componenteNome,
                historico.getTipo(),
                historico.getQuantidade(),
                historico.getUsuario(),
                historico.getDataHora(),
                historico.getCodigoMovimentacao()
        );
    }
}