package com.example.Back.Service;

import com.example.Back.Dto.CategoriaStatsDTO;
import com.example.Back.Dto.ComponenteDTO;
import com.example.Back.Entity.Usuario; // 1. IMPORTAR
import com.example.Back.Repository.ComponenteRepository;
import com.example.Back.Repository.PedidoCompraRepository;
import com.example.Back.Repository.RequisicaoRepository;
import org.springframework.security.core.context.SecurityContextHolder; // 2. IMPORTAR
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ComponenteRepository componenteRepository;
    private final RequisicaoRepository requisicaoRepository;
    private final PedidoCompraRepository pedidoCompraRepository;

    public DashboardService(ComponenteRepository componenteRepository,
                            RequisicaoRepository requisicaoRepository,
                            PedidoCompraRepository pedidoCompraRepository) {
        this.componenteRepository = componenteRepository;
        this.requisicaoRepository = requisicaoRepository;
        this.pedidoCompraRepository = pedidoCompraRepository;
    }

    // (Este método está 100% correto)
    public List<CategoriaStatsDTO> getCategoriaStats() {
        return componenteRepository.getCategoriaStats();
    }

    // (Este método está 100% correto)
    public List<ComponenteDTO> getItensEstoqueBaixo() {
        return componenteRepository.findItensEstoqueBaixo()
                .stream()
                .map(ComponenteDTO::new)
                .collect(Collectors.toList());
    }

    // ❌ REMOVA o método 'getKpiCards' (nome errado)
    // public Map<String, Long> getKpiCards() { ... }

    // ✅ ESTE É O MÉTODO CORRETO (que o Controller chama)
    public Map<String, Long> getKpis() {
        // 3. Pega o usuário logado e seu domínio
        Usuario usuarioLogado = getAuthenticatedUser();
        String dominio = usuarioLogado.getDominio();

        Map<String, Long> kpis = new HashMap<>();

        // 4. Contagens de Componentes (são globais, não têm domínio)
        long totalItens = componenteRepository.count();
        long itensEstoqueBaixo = componenteRepository.countItensEstoqueBaixo();

        // 5. Contagens de Requisições e Pedidos (FILTRADAS por domínio)
        long requisicoesPendentes = requisicaoRepository.countByStatusAndUsuarioDominio("PENDENTE", dominio);
        long pedidosPendentes = pedidoCompraRepository.countByStatusAndUsuarioDominio("PENDENTE", dominio);

        kpis.put("totalItensCadastrados", totalItens);
        kpis.put("itensComEstoqueBaixo", itensEstoqueBaixo);
        kpis.put("requisicoesPendentes", requisicoesPendentes);
        kpis.put("pedidosCompraPendentes", pedidosPendentes);

        return kpis;
    }

    // 6. Adicione o helper para pegar o usuário (copiado de outros services)
    private Usuario getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        // Isso não deve acontecer se o SecurityFilter estiver correto
        throw new RuntimeException("Nenhum usuário autenticado encontrado.");
    }
}