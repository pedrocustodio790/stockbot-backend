package com.example.Back.Service;

import com.example.Back.Dto.ComponenteDTO; // Importar DTO
import com.example.Back.Dto.DashboardKpisDTO;
import com.example.Back.Dto.DashboardStatsDTO;
import com.example.Back.Entity.Usuario;
import com.example.Back.Repository.ComponenteRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ComponenteRepository componenteRepository;

    public DashboardService(ComponenteRepository componenteRepository) {
        this.componenteRepository = componenteRepository;
    }

    // Helper para pegar domínio (Perfeito)
    private String getDominioAtual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario.getDominio();
        }
        throw new RuntimeException("Erro: Usuário não autenticado corretamente.");
    }

    @Transactional(readOnly = true)
    public DashboardKpisDTO getKpis() {
        String dominio = getDominioAtual();

        // Lembre-se: O repositório DEVE usar COALESCE(SUM(...), 0) para não quebrar se estiver vazio
        long totalItens = componenteRepository.countByDominio(dominio);
        long totalUnidades = componenteRepository.sumQuantidadeByDominio(dominio);
        long itensEmFalta = componenteRepository.countItensEmFaltaByDominio(dominio);

        return new DashboardKpisDTO(totalItens, totalUnidades, itensEmFalta);
    }

    @Transactional(readOnly = true)
    public List<DashboardStatsDTO> getStatsPorCategoria() {
        String dominio = getDominioAtual();
        return componenteRepository.countByCategoriaGrouped(dominio);
    }

    // MUDANÇA: Retorna DTO em vez de Entidade
    @Transactional(readOnly = true)
    public List<ComponenteDTO> getItensEstoqueBaixo() {
        String dominio = getDominioAtual();

        return componenteRepository.findEstoqueBaixoByDominio(dominio)
                .stream()
                .map(ComponenteDTO::new) // Converte Entidade -> DTO
                .collect(Collectors.toList());
    }
}
