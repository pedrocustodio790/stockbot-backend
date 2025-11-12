package com.example.Back.Controller;

import com.example.Back.Dto.AcaoRequestDTO;
import com.example.Back.Dto.PedidoCompraCreateDTO;
import com.example.Back.Dto.PedidoCompraDTO;
import com.example.Back.Entity.PedidoCompra;
import com.example.Back.Entity.Usuario;
import com.example.Back.Service.PedidoCompraService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// import java.util.List; // MUDANÇA: Não é mais necessário

@RestController
@RequestMapping("/api/pedidos-compra")
public class PedidoCompraController {

    private final PedidoCompraService pedidoCompraService;

    public PedidoCompraController(PedidoCompraService pedidoCompraService) {
        this.pedidoCompraService = pedidoCompraService;
    }

    // (Este método está perfeito)
    @PostMapping
    public ResponseEntity<Void> createPedido(@RequestBody @Valid PedidoCompraCreateDTO dto) {
        pedidoCompraService.createPedido(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // --- MÉTODO OTIMIZADO (Paginação) ---
    @GetMapping("/me")
    public ResponseEntity<Page<PedidoCompraDTO>> getMeusPedidos(Pageable pageable) { // MUDANÇA: Recebe Pageable
        // MUDANÇA: Passa o pageable para o service
        return ResponseEntity.ok(pedidoCompraService.findMeusPedidos(pageable));
    }

    // (Este método já estava perfeito)
    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PedidoCompraDTO>> getPendentes(Pageable pageable) {
        return ResponseEntity.ok(pedidoCompraService.findPendentes(pageable));
    }

    // (Este método está perfeito)
    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoCompraDTO> aprovarPedido(
            @PathVariable Long id,
            @RequestBody @Valid AcaoRequestDTO dto,
            Authentication authentication
    ) {
        Usuario adminLogado = (Usuario) authentication.getPrincipal();
        PedidoCompra pedidoAtualizado = pedidoCompraService.aprovarPedido(id, dto.getMotivo(), adminLogado);
        return ResponseEntity.ok(new PedidoCompraDTO(pedidoAtualizado));
    }

    // (Este método está perfeito)
    @PutMapping("/{id}/recusar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoCompraDTO> recusarPedido(
            @PathVariable Long id,
            @RequestBody @Valid AcaoRequestDTO dto,
            Authentication authentication
    ) {
        Usuario adminLogado = (Usuario) authentication.getPrincipal();
        PedidoCompra pedidoAtualizado = pedidoCompraService.recusarPedido(id, dto.getMotivo(), adminLogado);
        return ResponseEntity.ok(new PedidoCompraDTO(pedidoAtualizado));
    }
}