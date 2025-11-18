package com.example.Back.Controller;

// Importe os DTOs e Entidades corretos (aqueles que o Service usa)
import com.example.Back.Dto.DashboardKpisDTO;
import com.example.Back.Dto.DashboardStatsDTO;
import com.example.Back.Entity.Componente;
import com.example.Back.Service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")

public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // 1. Endpoint KPIs (Corrigido o Tipo de Retorno)
    @GetMapping("/kpis")
    @PreAuthorize("isAuthenticated()") // Permite ADMIN e USER
    public ResponseEntity<DashboardKpisDTO> getKpis() {
        // Agora o tipo bate com o que o Service retorna
        return ResponseEntity.ok(dashboardService.getKpis());
    }

    // 2. Endpoint Gráfico (Corrigido Nome do Método e DTO)
    @GetMapping("/stats-categorias")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DashboardStatsDTO>> getStatsCategorias() {
        // Chamando o método certo: getStatsPorCategoria
        return ResponseEntity.ok(dashboardService.getStatsPorCategoria());
    }

    // 3. Endpoint Estoque Baixo (Corrigido para aceitar a Entidade Componente)
    @GetMapping("/estoque-baixo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Componente>> getItensEstoqueBaixo() {
        // O Service retorna List<Componente>, então o Controller aceita isso
        return ResponseEntity.ok(dashboardService.getItensEstoqueBaixo());
    }
}