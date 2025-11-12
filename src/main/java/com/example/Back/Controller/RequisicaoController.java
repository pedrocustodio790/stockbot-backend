package com.example.Back.Controller;

// Importe os DTOs que você VAI PRECISAR
import com.example.Back.Dto.AcaoRequestDTO;
import com.example.Back.Dto.RequisicaoCreateDTO;
import com.example.Back.Dto.RequisicaoDTO; // <-- DTO para a lista
import com.example.Back.Entity.Usuario;
import com.example.Back.Service.RequisicaoService;
import jakarta.validation.Valid;

// Imports para paginação e segurança
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // <-- O import CORRETO
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requisicoes") // A base da URL
public class RequisicaoController {

    private final RequisicaoService requisicaoService;

    // O seu construtor (correto)
    public RequisicaoController(RequisicaoService requisicaoService) {
        this.requisicaoService = requisicaoService;
    }

    // --- Endpoint para o USUÁRIO criar a requisição ---
    @PostMapping
    public ResponseEntity<Void> createRequisicao(@RequestBody @Valid RequisicaoCreateDTO dto) {
        requisicaoService.createRequisicao(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // --- Endpoint para o ADMIN ver as pendentes ---
    // (✅ ESTE É O QUE CORRIGE O SEU ERRO 403/404)
    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RequisicaoDTO>> getPendentes(Pageable pageable) {
        return ResponseEntity.ok(requisicaoService.findPendentes(pageable));
    }

    // --- Endpoint para o ADMIN aprovar ---
    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RequisicaoDTO> aprovarRequisicao(
            @PathVariable Long id,
            @RequestBody @Valid AcaoRequestDTO dto, // Recebe o "motivo"
            Authentication authentication // Recebe o admin logado
    ) {
        Usuario adminLogado = (Usuario) authentication.getPrincipal();
        RequisicaoDTO requisicaoAtualizada = requisicaoService.aprovarRequisicao(id, dto.getMotivo(), adminLogado);
        return ResponseEntity.ok(requisicaoAtualizada);
    }

    // --- Endpoint para o ADMIN recusar ---
    @PutMapping("/{id}/recusar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RequisicaoDTO> recusarRequisicao(
            @PathVariable Long id,
            @RequestBody @Valid AcaoRequestDTO dto, // Recebe o "motivo"
            Authentication authentication // Recebe o admin logado
    ) {
        Usuario adminLogado = (Usuario) authentication.getPrincipal();
        RequisicaoDTO requisicaoAtualizada = requisicaoService.recusarRequisicao(id, dto.getMotivo(), adminLogado);
        return ResponseEntity.ok(requisicaoAtualizada);
    }
}