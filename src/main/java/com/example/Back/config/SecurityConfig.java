package com.example.Back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List; // <--- Certifique-se de importar List

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // --- CORREÇÃO 1: OBRIGATÓRIO PARA PREFLIGHT DO NAVEGADOR ---
                        // Libera o "sinal" (OPTIONS) que o navegador manda antes do POST
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --- Rotas Públicas ---
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers("/user-photos/**").permitAll()

                        // --- Rotas de Dashboard (Acesso geral) ---
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/**").authenticated()

                        // --- Rotas de ADMIN ---
                        // DICA: Se seu banco salva "ADMIN", use hasAuthority("ADMIN").
                        // Se salva "ROLE_ADMIN", use hasRole("ADMIN"). Mantenha hasRole se tiver certeza.
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/configuracoes/**").hasRole("ADMIN")
                        .requestMatchers("/api/requisicoes/pendentes").hasRole("ADMIN")
                        .requestMatchers("/api/requisicoes/*/aprovar").hasRole("ADMIN")
                        .requestMatchers("/api/requisicoes/*/recusar").hasRole("ADMIN")
                        .requestMatchers("/api/pedidos-compra/pendentes").hasRole("ADMIN")
                        .requestMatchers("/api/pedidos-compra/*/aprovar").hasRole("ADMIN")
                        .requestMatchers("/api/pedidos-compra/*/recusar").hasRole("ADMIN")

                        // --- Qualquer outra rota ---
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // --- CORREÇÃO 2: "BALA DE PRATA" PARA O RENDER ---
        // Em vez de listar URLs exatas (que falham por causa de http vs https ou barra no final),
        // usamos o padrão curinga. Isso resolve 99% dos erros de bloqueio no Render.
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Se preferir manter sua lista restrita, certifique-se que a URL do front
        // NÃO tem barra no final. Mas o padrão acima é mais seguro para evitar dor de cabeça agora.
        /* configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:8081",
                "https://stockbot-2xyv.onrender.com"
        ));
        */

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}