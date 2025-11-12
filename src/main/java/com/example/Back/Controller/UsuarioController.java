package com.example.Back.Controller;

import com.example.Back.Dto.*;
import com.example.Back.Entity.Usuario;
import com.example.Back.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page; // MUDANÇA: Importar Page
import org.springframework.data.domain.Pageable; // MUDANÇA: Importar Pageable
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// import java.util.List; // MUDANÇA: Não usamos mais List

@RestController
@RequestMapping("/api/users")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // --- MÉTODO OTIMIZADO (Paginação) ---
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsuarioDTO>> getAllUsers(Pageable pageable) { // MUDANÇA: Recebe Pageable
        // MUDANÇA: Chama o service paginado e retorna a Page
        return ResponseEntity.ok(usuarioService.findAll(pageable));
    }

    // --- MÉTODO CORRIGIDO (Segurança) ---
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDTO> getMyProfile(Authentication authentication) { // MUDANÇA: Retorna DTO
        String userEmail = authentication.getName();
        Usuario usuario = usuarioService.findByEmail(userEmail); // Busca a entidade

        // MUDANÇA: Converte a Entidade para DTO manualmente
        // Isso evita vazar a senha e outros campos
        UsuarioDTO dto = new UsuarioDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getNome(),
                usuario.getCaminhoFotoPerfil()
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> createUser(@RequestBody @Valid CreateUserDTO createUserDTO) {
        UsuarioDTO novoUsuario = usuarioService.createUser(createUserDTO);
        return new ResponseEntity<>(novoUsuario, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> changeUserRole(@PathVariable Long id, @RequestBody UpdateRoleDTO dto) {
        return ResponseEntity.ok(usuarioService.changeUserRole(id, dto.role()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        usuarioService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changeCurrentUserPassword(
            Authentication authentication,
            @RequestBody @Valid PasswordChangeDTO passwordChangeDTO
    ) {
        try {
            String userEmail = authentication.getName();
            usuarioService.changePassword(userEmail, passwordChangeDTO);
            return ResponseEntity.ok("Senha alterada com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetUserPassword(
            @PathVariable Long id,
            @RequestBody @Valid PasswordResetDTO passwordResetDTO
    ) {
        usuarioService.resetPassword(id, passwordResetDTO);
        return ResponseEntity.ok().build();
    }
}