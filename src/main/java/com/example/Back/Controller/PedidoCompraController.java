package com.example.Back.Controller;

import com.example.Back.Dto.AcaoRequestDTO;
import com.example.Back.Dto.PedidoCompraCreateDTO;
import com.example.Back.Dto.PedidoCompraDTO;
import com.example.Back.Entity.PedidoCompra;
import com.example.Back.Entity.Usuario;
import com.example.Back.Service.PedidoCompraService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos-compra")
public class PedidoCompraController {

    private final PedidoCompraService pedidoCompraService;

    public PedidoCompraController(PedidoCompraService pedidoCompraService) {
        this.pedidoCompraService = pedidoCompraService;
    }

    // Endpoint para o USUÁRIO criar um pedido
    @PostMapping
    public ResponseEntity<Void> createPedido(@RequestBody @Valid PedidoCompraCreateDTO dto) {
        pedidoCompraService.createPedido(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // Endpoint para o USUÁRIO ver seus pedidos
    @GetMapping("/me")
    public ResponseEntity<List<PedidoCompraDTO>> getMeusPedidos() {
        return ResponseEntity.ok(pedidoCompraService.findMeusPedidos());
    }

    // Endpoint para o ADMIN ver pedidos pendentes
    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PedidoCompraDTO>> getPendentes(Pageable pageable) {
        return ResponseEntity.ok(pedidoCompraService.findPendentes(pageable));
    }

    // Endpoint para o ADMIN aprovar
    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoCompraDTO> aprovarPedido( // ✅ 2. MUDOU O RETORNO
                                                          @PathVariable Long id,
                                                          @RequestBody @Valid AcaoRequestDTO dto, // ✅ 3. RECEBE O MOTIVO
                                                          Authentication authentication // ✅ 4. RECEBE O ADMIN LOGADO
    ) {
        // Pega o usuário (admin) que está fazendo a chamada
        Usuario adminLogado = (Usuario) authentication.getPrincipal();

        // Chama o service atualizado
        PedidoCompra pedidoAtualizado = pedidoCompraService.aprovarPedido(id, dto.getMotivo(), adminLogado);

        // Retorna o pedido completo com os dados da auditoria
        return ResponseEntity.ok(new PedidoCompraDTO(pedidoAtualizado));
    }

    // Endpoint para o ADMIN recusar
    @PutMapping("/{id}/recusar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoCompraDTO> recusarPedido( // ✅ 2. MUDOU O RETORNO
                                                          @PathVariable Long id,
                                                          @RequestBody @Valid AcaoRequestDTO dto, // ✅ 3. RECEBE O MOTIVO
                                                          Authentication authentication // ✅ 4. RECEBE O ADMIN LOGADO
    ) {
        Usuario adminLogado = (Usuario) authentication.getPrincipal();
        PedidoCompra pedidoAtualizado = pedidoCompraService.recusarPedido(id, dto.getMotivo(), adminLogado);
        return ResponseEntity.ok(new PedidoCompraDTO(pedidoAtualizado));
    }
}