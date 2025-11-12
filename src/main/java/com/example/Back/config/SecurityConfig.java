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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    // ✅ Bean do SecurityFilterChain (Configuração das regras de acesso)
    // ✅ Bean do SecurityFilterChain (Configuração das regras de acesso)
// Em: src/main/java/com/example/Back/config/SecurityConfig.java

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // --- Rotas Públicas ---
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers("/user-photos/**").permitAll() // Fotos públicas

                        // --- Rotas de Usuário Autenticado ---
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated() // Ver próprio perfil
                        .requestMatchers(HttpMethod.POST, "/api/requisicoes").authenticated() // Criar requisição de estoque
                        .requestMatchers(HttpMethod.POST, "/api/pedidos-compra").authenticated() // Criar pedido de compra
                        .requestMatchers(HttpMethod.GET, "/api/pedidos-compra/me").authenticated() // Ver próprios pedidos
                        .requestMatchers("/api/componentes/**").authenticated() // Ver/Buscar componentes
                        .requestMatchers(HttpMethod.GET, "/api/configuracoes/limiteEstoqueBaixo").authenticated() // Ver limite

                        // --- Rotas de ADMIN ---
                        // (Regras específicas vêm ANTES das genéricas)
                        .requestMatchers(HttpMethod.GET, "/api/requisicoes/pendentes").hasRole("ADMIN") // ✅ REGRA ADICIONADA AQUI E NA ORDEM CERTA
                        .requestMatchers(HttpMethod.PUT, "/api/requisicoes/{id}/aprovar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/requisicoes{id}/recusar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/pedidos-compra/pendentes").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos-compra/{id}/aprovar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos-compra/{id}/recusar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/{id}/reset-password").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN") // Regra genérica para /users
                        .requestMatchers("/api/configuracoes/**").hasRole("ADMIN") // Regra genérica para /configuracoes

                        // --- Qualquer outra rota ---
                        .anyRequest().authenticated() // Exige autenticação por padrão
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // ✅ Bean de configuração do CORS (Quem pode acessar a API)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite requisições das origens dos seus frontends
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173", // Seu frontend WEB
                "http://localhost:8081"  // Seu frontend MOBILE (Expo Web) - ADICIONADO
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // Permite todos os headers
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica a todos os endpoints
        return source;
    }

    // ✅ Bean do AuthenticationManager (Quem gerencia a autenticação)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // ✅ Bean do PasswordEncoder (Como criptografar senhas)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}