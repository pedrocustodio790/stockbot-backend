package com.example.Back.Controller;

import com.example.Back.Dto.ComponenteDTO; // Importar
import com.example.Back.Dto.DashboardKpisDTO;
import com.example.Back.Dto.DashboardStatsDTO;
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

    @GetMapping("/kpis")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardKpisDTO> getKpis() {
        return ResponseEntity.ok(dashboardService.getKpis());
    }

    @GetMapping("/stats-categorias")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DashboardStatsDTO>> getStatsCategorias() {
        return ResponseEntity.ok(dashboardService.getStatsPorCategoria());
    }

    // MUDANÇA: Tipo de retorno List<ComponenteDTO>
    @GetMapping("/estoque-baixo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ComponenteDTO>> getItensEstoqueBaixo() {
        return ResponseEntity.ok(dashboardService.getItensEstoqueBaixo());
    }
}