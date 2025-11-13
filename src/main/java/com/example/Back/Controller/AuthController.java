package com.example.Back.Controller;

// 1. IMPORTAR OS DTOs CORRETOS
import com.example.Back.Dto.LoginDTO;
import com.example.Back.Dto.LoginResponseDTO;
import com.example.Back.Dto.RegisterDTO;
import com.example.Back.Dto.UsuarioDTO; // Importar o DTO de usuário
import com.example.Back.Entity.Usuario;
import com.example.Back.Service.AuthService;
import com.example.Back.Service.TokenService; // Importar o TokenService
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // Importar o AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // 2. INJETAR OS SERVIÇOS NECESSÁRIOS
    private final AuthService authService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager; // O "porteiro" do Spring

    public AuthController(AuthService authService, TokenService tokenService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    // --- 3. MÉTODO DE LOGIN (CORRIGIDO E ATUALIZADO) ---
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginDTO dto) {

        // 3a. Cria a string "email::dominio" que o AuthService espera
        String combinedUsername = dto.email() + "::" + dto.dominio();

        // 3b. Usa o AuthenticationManager para validar
        var authenticationToken = new UsernamePasswordAuthenticationToken(combinedUsername, dto.senha());
        var authentication = authenticationManager.authenticate(authenticationToken);

        // 3c. Se chegou aqui, o login é válido. Pegue o usuário e crie o token.
        var usuario = (Usuario) authentication.getPrincipal();
        var tokenJWT = tokenService.createToken(usuario);

        // 3d. Converte o usuário para DTO e retorna o novo LoginResponseDTO
        UsuarioDTO usuarioDTO = new UsuarioDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getNome(),
                usuario.getCaminhoFotoPerfil(),
                usuario.getDominio() // Incluindo o domínio
        );

        return ResponseEntity.ok(new LoginResponseDTO(tokenJWT, usuarioDTO));
    }

    // --- 4. MÉTODO DE REGISTRO (CORRIGIDO E LIMPO) ---
    // (Não aceita mais MultipartFile. O upload é feito em outro lugar.)
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterDTO dto) {
        // A lógica de checagem, criptografia e salvar
        // está toda dentro do AuthService
        authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}