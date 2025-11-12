package com.example.Back.Controller;

import com.example.Back.Dto.AuthDTO;
import com.example.Back.Dto.LoginResponseDTO;
// import com.example.Back.Dto.RegisterDTO; // Não está sendo usado
import com.example.Back.Dto.UsuarioDTO; // MUDANÇA: Importar o DTO de usuário
import com.example.Back.Entity.Usuario;
import com.example.Back.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthDTO data) {
        String token = authService.login(data);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    // --- MÉTODO CORRIGIDO (Segurança) ---
    @PostMapping("/register")
    public ResponseEntity<?> register( // MUDANÇA: Retorna DTO, não Entidade
                                       @RequestParam("nome") String nome,
                                       @RequestParam("email") String email,
                                       @RequestParam("senha") String senha,
                                       @RequestPart(value = "fotoPerfil", required = false) MultipartFile fotoPerfil
    ) {
        try {
            // 1. O Service ainda retorna a Entidade (o que é ok)
            Usuario novoUsuario = authService.register(nome, email, senha, fotoPerfil);

            // 2. O Controller converte a Entidade para um DTO seguro
            UsuarioDTO usuarioDTO = new UsuarioDTO(
                    novoUsuario.getId(),
                    novoUsuario.getEmail(),
                    novoUsuario.getRole(),
                    novoUsuario.getNome(),
                    novoUsuario.getCaminhoFotoPerfil()
            );

            // 3. O Controller retorna o DTO (sem a senha)
            return new ResponseEntity<>(usuarioDTO, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}