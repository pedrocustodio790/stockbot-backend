package com.example.Back.Controller;

import com.example.Back.Dto.AcaoRequestDTO;
import com.example.Back.Dto.RequisicaoCreateDTO;
import com.example.Back.Dto.RequisicaoDTO;
import com.example.Back.Service.RequisicaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requisicoes")
public class RequisicaoController {

    private final RequisicaoService requisicaoService;

    public RequisicaoController(RequisicaoService requisicaoService) {
        this.requisicaoService = requisicaoService;
    }

    @PostMapping
    public ResponseEntity<Void> createRequisicao(@RequestBody @Valid RequisicaoCreateDTO dto) {
        requisicaoService.createRequisicao(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RequisicaoDTO>> getPendentes(Pageable pageable) {
        return ResponseEntity.ok(requisicaoService.findPendentes(pageable));
    }

    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RequisicaoDTO> aprovarRequisicao(
            @PathVariable Long id,
            @RequestBody @Valid AcaoRequestDTO dto
    ) {
        // O service pega o admin logado automaticamente
        RequisicaoDTO requisicao = requisicaoService.aprovarRequisicao(id, dto.getMotivo());
        return ResponseEntity.ok(requisicao);
    }

    @PutMapping("/{id}/recusar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RequisicaoDTO> recusarRequisicao(
            @PathVariable Long id,
            @RequestBody @Valid AcaoRequestDTO dto
    ) {
        RequisicaoDTO requisicao = requisicaoService.recusarRequisicao(id, dto.getMotivo());
        return ResponseEntity.ok(requisicao);
    }
}