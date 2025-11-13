package com.example.Back.Service;

import com.example.Back.Dto.PasswordChangeDTO;
import com.example.Back.Dto.PasswordResetDTO;
import com.example.Back.Dto.RegisterDTO; // MUDANÇA: Usando o DTO de Registro
import com.example.Back.Dto.UsuarioDTO;
import com.example.Back.Entity.UserRole;
import com.example.Back.Entity.Usuario;
import com.example.Back.Repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // --- NOVO MÉTODO HELPER ---
    // Pega o usuário (com domínio) que está logado no momento
    private Usuario getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        throw new RuntimeException("Nenhum usuário autenticado encontrado.");
    }

    // --- MÉTODO ATUALIZADO (Seguro) ---
    // Mostra apenas os usuários do *mesmo domínio* do Admin logado.
    @Transactional(readOnly = true)
    public Page<UsuarioDTO> findAll(Pageable pageable) {
        String dominio = getAuthenticatedUser().getDominio();
        return usuarioRepository.findAllByDominio(dominio, pageable)
                .map(this::toDTO);
    }

    // --- MÉTODO ATUALIZADO (Seguro) ---
    // (Este método é para um Admin criar um usuário)
    public UsuarioDTO createUser(RegisterDTO dto) {
        // Um admin só pode criar usuários no seu próprio domínio
        String adminDominio = getAuthenticatedUser().getDominio();

        if (usuarioRepository.findByEmailAndDominio(dto.email(), adminDominio).isPresent()) {
            throw new IllegalArgumentException("Erro: E-mail já está em uso neste domínio!");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setEmail(dto.email());
        novoUsuario.setSenha(passwordEncoder.encode(dto.senha()));
        novoUsuario.setRole(dto.role());
        novoUsuario.setNome(dto.nome());
        novoUsuario.setDominio(adminDominio); // Seta o domínio do Admin

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
        return toDTO(usuarioSalvo);
    }

    // --- MÉTODO ATUALIZADO (Seguro) ---
    // Garante que o Admin só pode mudar roles do seu próprio domínio
    @Transactional
    public UsuarioDTO changeUserRole(Long userId, UserRole newRole) {
        String adminDominio = getAuthenticatedUser().getDominio();

        Usuario usuario = usuarioRepository.findByIdAndDominio(userId, adminDominio)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado neste domínio."));

        usuario.setRole(newRole);
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return toDTO(usuarioSalvo);
    }

    // --- MÉTODO ATUALIZADO (Seguro) ---
    // Garante que o Admin só pode deletar usuários do seu próprio domínio
    public void deleteUser(Long id) {
        String adminDominio = getAuthenticatedUser().getDominio();

        if (!usuarioRepository.existsByIdAndDominio(id, adminDominio)) {
            throw new RuntimeException("Utilizador não encontrado neste domínio com o id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // --- MÉTODO ATUALIZADO (Seguro) ---
    // (Este é para o *próprio* usuário mudar sua senha)
    @Transactional
    public void changePassword(PasswordChangeDTO dto) {
        // Pega o usuário logado (email E domínio)
        Usuario usuario = getAuthenticatedUser();

        if (!passwordEncoder.matches(dto.getCurrentPassword(), usuario.getSenha())) {
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("A nova senha deve ter no mínimo 6 caracteres.");
        }

        usuario.setSenha(passwordEncoder.encode(dto.getNewPassword()));
        usuarioRepository.save(usuario);
    }

    // --- MÉTODO ATUALIZADO (Seguro) ---
    // (Este é para um *Admin* resetar a senha de outro usuário)
    @Transactional
    public void resetPassword(Long userId, PasswordResetDTO dto) {
        String adminDominio = getAuthenticatedUser().getDominio();

        Usuario user = usuarioRepository.findByIdAndDominio(userId, adminDominio)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado neste domínio com ID: " + userId));

        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());
        user.setSenha(encodedPassword);
        usuarioRepository.save(user);
    }

    // --- MÉTODO ATUALIZADO (DTO) ---
    // O DTO agora precisa do 'dominio'
    private UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getNome(),
                usuario.getCaminhoFotoPerfil(),
                usuario.getDominio() // ✅ Campo adicionado
        );
    }

    // ❌ O método 'findByEmail(String email)' foi REMOVIDO
    //    pois era inseguro e foi substituído pelo helper 'getAuthenticatedUser()'
}