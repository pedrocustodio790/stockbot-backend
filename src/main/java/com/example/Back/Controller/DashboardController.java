package com.example.Back.Controller;

import com.example.Back.Dto.CategoriaStatsDTO;
import com.example.Back.Dto.ComponenteDTO;
import com.example.Back.Service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')") // Só Admins podem ver o dashboard
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // 1. Endpoint para os 4 cards do topo
    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Long>> getKpis() {
        return ResponseEntity.ok(dashboardService.getKpis());
    }

    // 2. Endpoint para o gráfico de categorias (o "Chart.js")
    @GetMapping("/stats-categorias")
    public ResponseEntity<List<CategoriaStatsDTO>> getCategoriaStats() {
        return ResponseEntity.ok(dashboardService.getCategoriaStats());
    }

    // 3. Endpoint para a lista de "Estoque Baixo" (o "ActionList")
    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<ComponenteDTO>> getItensEstoqueBaixo() {
        return ResponseEntity.ok(dashboardService.getItensEstoqueBaixo());
    }
}