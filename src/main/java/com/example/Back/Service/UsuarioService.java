package com.example.Back.Service;

import com.example.Back.Dto.CreateUserDTO;
import com.example.Back.Dto.PasswordChangeDTO;
import com.example.Back.Dto.PasswordResetDTO;
import com.example.Back.Dto.UsuarioDTO;
import com.example.Back.Entity.UserRole;
import com.example.Back.Entity.Usuario;
import com.example.Back.Repository.UsuarioRepository;
import org.springframework.data.domain.Page; // 1. IMPORTAR
import org.springframework.data.domain.Pageable; // 2. IMPORTAR
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- MÉTODO OTIMIZADO ---
    @Transactional(readOnly = true)
    public Page<UsuarioDTO> findAll(Pageable pageable) {
        // 3. CHAMA O findAll PAGINADO
        return usuarioRepository.findAll(pageable)
                .map(this::toDTO); // 4. CONVERTE A PÁGINA
    }

    // (Este método está perfeito)
    public UsuarioDTO createUser(CreateUserDTO createUserDTO) {
        if (usuarioRepository.findByEmail(createUserDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Erro: E-mail já está em uso!");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setEmail(createUserDTO.getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(createUserDTO.getSenha()));
        novoUsuario.setRole(createUserDTO.getRole());

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
        return toDTO(usuarioSalvo);
    }

    // (Este método está perfeito)
    @Transactional
    public UsuarioDTO changeUserRole(Long userId, UserRole newRole) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado."));
        usuario.setRole(newRole);
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return toDTO(usuarioSalvo);
    }

    // (Este método está perfeito)
    public void deleteUser(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Utilizador não encontrado com o id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // (Este método está perfeito)
    @Transactional
    public void changePassword(String userEmail, PasswordChangeDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado."));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), usuario.getSenha())) {
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("A nova senha deve ter no mínimo 6 caracteres.");
        }

        usuario.setSenha(passwordEncoder.encode(dto.getNewPassword()));
        usuarioRepository.save(usuario);
    }

    // (Este método está perfeito)
    @Transactional
    public void resetPassword(Long userId, PasswordResetDTO dto) {
        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com ID: " + userId));

        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());
        user.setSenha(encodedPassword);
        usuarioRepository.save(user);
    }

    // (Este método está perfeito)
    private UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getNome(),
                usuario.getCaminhoFotoPerfil()
        );
    }

    // 5. REMOVEMOS 'findAllUsers()' e 'updateUserRole()' por serem duplicatas ruins

    // (Este método está perfeito)
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + email));
    }
}