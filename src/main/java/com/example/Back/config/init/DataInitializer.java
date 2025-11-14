package com.example.Back.config.init;

import com.example.Back.Entity.Usuario;
import com.example.Back.Entity.UserRole;
import com.example.Back.Repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;


    private static final String ADMIN_EMAIL = "admin@stockbot.com";
    private static final String ADMIN_DOMINIO = "principal"; // O domínio "mestre"
    private static final String ADMIN_SENHA = "admin123";

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        // MUDANÇA: Verifica usando o método novo e seguro
        if (usuarioRepository.findByEmailAndDominio(ADMIN_EMAIL, ADMIN_DOMINIO).isEmpty()) {

            System.out.println("Nenhum usuário admin padrão encontrado, criando um novo...");

            Usuario admin = new Usuario();
            admin.setEmail(ADMIN_EMAIL);
            admin.setSenha(passwordEncoder.encode(ADMIN_SENHA));
            admin.setRole(UserRole.ADMIN);
            admin.setNome("Admin Padrão");

            // MUDANÇA: Seta o novo campo obrigatório
            admin.setDominio(ADMIN_DOMINIO);

            usuarioRepository.save(admin);

            System.out.println("Usuário admin padrão (" + ADMIN_EMAIL + " / " + ADMIN_DOMINIO + ") criado com sucesso!");
        } else {
            System.out.println("Usuário admin padrão (" + ADMIN_EMAIL + ") já existe.");
        }
    }
}