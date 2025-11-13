package com.example.Back.Service;

import com.example.Back.Dto.RegisterDTO; // MUDANÇA: Importar o DTO correto
import com.example.Back.Entity.Usuario;
import com.example.Back.Repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // MUDANÇA: Este método agora é chamado pelo Spring Security (via AuthController)
    // Ele recebe a string "email::dominio"
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Separa a string
        String[] parts = username.split("::");
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Formato de login inválido (esperado: email::dominio).");
        }

        String email = parts[0];
        String dominio = parts[1];

        // 2. Busca no repositório pelo método novo e correto
        return usuarioRepository.findByEmailAndDominio(email, dominio)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário, senha ou domínio inválidos."));
    }

    // MUDANÇA: O método de REGISTRO foi 100% substituído
    public void register(RegisterDTO dto) {

        // 1. Verifica se a combinação email+dominio já existe
        if (usuarioRepository.findByEmailAndDominio(dto.email(), dto.dominio()).isPresent()) {
            throw new RuntimeException("Este e-mail já está cadastrado para este domínio.");
        }

        // 2. Cria o novo usuário a partir do DTO
        Usuario novoUsuario = new Usuario();
        novoUsuario.setEmail(dto.email());
        novoUsuario.setSenha(passwordEncoder.encode(dto.senha())); // Criptografa a senha
        novoUsuario.setRole(dto.role());
        novoUsuario.setNome(dto.nome());
        novoUsuario.setDominio(dto.dominio()); // Salva o domínio

        // 3. Salva no banco
        usuarioRepository.save(novoUsuario);
    }
}

