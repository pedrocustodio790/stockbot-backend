package com.example.Back.Controller;

import com.example.Back.Dto.ComponenteDTO;
import com.example.Back.Service.ComponenteService;
import org.springframework.data.domain.Page; // MUDANÇA: Importar Page
import org.springframework.data.domain.Pageable; // MUDANÇA: Importar Pageable
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// import java.util.List; // MUDANÇA: Não usamos mais List aqui

@RestController
@RequestMapping("/api/componentes")
public class ComponenteController {

    private final ComponenteService componenteService;

    public ComponenteController(ComponenteService componenteService) {
        this.componenteService = componenteService;
    }

    // --- MÉTODO GET OTIMIZADO PARA PAGINAÇÃO ---
    @GetMapping
    public ResponseEntity<Page<ComponenteDTO>> getAllComponentes(
            @RequestParam(value = "termo", required = false) String termoDeBusca,
            Pageable pageable) { // MUDANÇA: Spring cria o Pageable a partir da URL

        // MUDANÇA: Passa o termo e o pageable para o service
        Page<ComponenteDTO> componentesPage = componenteService.findAll(termoDeBusca, pageable);

        // MUDANÇA: Retorna a Página (que contém os dados + info de paginação)
        return ResponseEntity.ok(componentesPage);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComponenteDTO> createComponente(@RequestBody ComponenteDTO componenteDTO) { // MUDANÇA: Tipo de retorno específico
        ComponenteDTO novoComponente = componenteService.create(componenteDTO);
        return ResponseEntity.ok(novoComponente);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComponenteDTO> updateComponente(@PathVariable Long id, @RequestBody ComponenteDTO componenteDTO) { // MUDANÇA: Tipo de retorno específico
        ComponenteDTO componenteAtualizado = componenteService.update(id, componenteDTO);
        return ResponseEntity.ok(componenteAtualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteComponente(@PathVariable Long id) { // MUDANÇA: Tipo de retorno específico
        componenteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}