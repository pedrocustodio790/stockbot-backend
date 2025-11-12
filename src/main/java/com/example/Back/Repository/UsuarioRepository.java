package com.example.Back.Repository;

import com.example.Back.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Mantenha APENAS este método para buscar por email.
    // Ele é seguro pois o Optional trata o caso de não encontrar o usuário.
    Optional<Usuario> findByEmail(String email);

    // O método findUserDetailsByEmail foi removido.
}