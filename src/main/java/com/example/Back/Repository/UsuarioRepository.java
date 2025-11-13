package com.example.Back.Repository;

import com.example.Back.Entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {


    Optional<Usuario> findByEmailAndDominio(String email, String dominio);


    @Query("SELECT u FROM Usuario u WHERE u.dominio = :dominio")
    Page<Usuario> findAllByDominio(String dominio, Pageable pageable);


    boolean existsByIdAndDominio(Long id, String dominio);


    Optional<Usuario> findByIdAndDominio(Long id, String dominio);

}