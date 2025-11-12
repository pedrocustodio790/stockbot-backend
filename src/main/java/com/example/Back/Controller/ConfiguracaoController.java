package com.example.Back.Controller;

import com.example.Back.Dto.ThresholdDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
// ✅ NOME DA ROTA CORRIGIDO
@RequestMapping("/api/configuracoes")
public class ConfiguracaoController { // ✅ NOME DA CLASSE CORRIGIDO

    private final AtomicInteger lowStockThreshold = new AtomicInteger(5);

    // ✅ NOME DO ENDPOINT CORRIGIDO
    @GetMapping("/limiteEstoqueBaixo")
    public ResponseEntity<Integer> getLowStockThreshold() {
        System.out.println("[CONFIG CONTROLLER] GET /limiteEstoqueBaixo - Retornando: " + lowStockThreshold.get());
        return ResponseEntity.ok(lowStockThreshold.get());
    }

    // ✅ NOME DO ENDPOINT CORRIGIDO
    @PutMapping("/limiteEstoqueBaixo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateLowStockThreshold(@RequestBody ThresholdDTO dto) {
        System.out.println("[CONFIG CONTROLLER] PUT /limiteEstoqueBaixo - Novo valor: " + dto.getThreshold());
        lowStockThreshold.set(dto.getThreshold());
        return ResponseEntity.ok().build();
    }
}