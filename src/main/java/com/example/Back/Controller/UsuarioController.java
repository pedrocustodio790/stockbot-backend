package com.example.Back.Controller;

import com.example.Back.Dto.*;
import com.example.Back.Entity.Usuario;
import com.example.Back.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // --- (Este método está 100% CORRETO) ---
    // O Service (findAll) já sabe filtrar pelo domínio do Admin logado
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsuarioDTO>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(usuarioService.findAll(pageable));
    }

    // --- MÉTODO CORRIGIDO (Segurança) ---
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDTO> getMyProfile(Authentication authentication) {
        // MUDANÇA: O 'principal' JÁ É o objeto Usuario completo
        // (Obrigado ao nosso SecurityFilter que vai fazer isso)
        Usuario usuario = (Usuario) authentication.getPrincipal();

        // MUDANÇA: Convertemos para DTO, agora incluindo o domínio
        UsuarioDTO dto = new UsuarioDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getNome(),
                usuario.getCaminhoFotoPerfil(),
                usuario.getDominio() // ✅ Incluído
        );
        return ResponseEntity.ok(dto);
    }

    // --- MÉTODO CORRIGIDO (DTO) ---
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    // MUDANÇA: Usa o RegisterDTO (que tem nome, email, senha, role, dominio)
    public ResponseEntity<UsuarioDTO> createUser(@RequestBody @Valid RegisterDTO registerDTO) {
        // O UsuarioService.createUser já foi atualizado para receber RegisterDTO
        UsuarioDTO novoUsuario = usuarioService.createUser(registerDTO);
        return new ResponseEntity<>(novoUsuario, HttpStatus.CREATED);
    }

    // --- (Este método está 100% CORRETO) ---
    // O Service (changeUserRole) já sabe checar o domínio
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> changeUserRole(@PathVariable Long id, @RequestBody UpdateRoleDTO dto) {
        return ResponseEntity.ok(usuarioService.changeUserRole(id, dto.role()));
    }

    // --- (Este método está 100% CORRETO) ---
    // O Service (deleteUser) já sabe checar o domínio
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        usuarioService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // --- MÉTODO CORRIGIDO (Lógica) ---
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changeCurrentUserPassword(
            // MUDANÇA: Não precisamos mais da 'Authentication' aqui
            @RequestBody @Valid PasswordChangeDTO passwordChangeDTO
    ) {
        try {
            // MUDANÇA: O service agora pega o usuário sozinho do Contexto
            usuarioService.changePassword(passwordChangeDTO);
            return ResponseEntity.ok("Senha alterada com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- (Este método está 100% CORRETO) ---
    // O Service (resetUserPassword) já sabe checar o domínio
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