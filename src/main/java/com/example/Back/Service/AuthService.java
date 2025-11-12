package com.example.Back.Service;

import com.example.Back.Dto.AuthDTO;
import com.example.Back.Entity.UserRole; // Import correto
import com.example.Back.Entity.Usuario;
import com.example.Back.Repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository, TokenService tokenService, PasswordEncoder passwordEncoder, FileStorageService fileStorageService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    public String login(AuthDTO data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senha());
            var auth = this.authenticationManager.authenticate(usernamePassword);
            return tokenService.gerarToken((Usuario) auth.getPrincipal());
        } catch (AuthenticationException e) {
            throw new RuntimeException("Falha na autenticação: e-mail ou senha inválidos.", e);
        }
    }

    public Usuario register(String nome, String email, String senha, MultipartFile fotoPerfil) {
        if (this.usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("E-mail já está em uso.");
        }

        String nomeArquivoFoto = null;
        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            nomeArquivoFoto = fileStorageService.save(fotoPerfil);
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(nome);
        novoUsuario.setEmail(email);
        novoUsuario.setSenha(passwordEncoder.encode(senha));
        novoUsuario.setCaminhoFotoPerfil(nomeArquivoFoto);
        novoUsuario.setRole(UserRole.USER); // ✅ CORRIGIDO

        return this.usuarioRepository.save(novoUsuario);
    }

    // O método register(RegisterDTO data) foi removido por ser redundante.
}