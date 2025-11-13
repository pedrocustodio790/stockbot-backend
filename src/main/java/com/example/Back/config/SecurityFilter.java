package com.example.Back.config;

import com.example.Back.Repository.UsuarioRepository;
import com.example.Back.Service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        var tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {
            try {
                // 1. Pega o EMAIL (Subject) do token
                var subject = tokenService.getSubject(tokenJWT);

                // 2. MUDANÇA: Pega o DOMÍNIO (Claim) do token
                var dominio = tokenService.getDominio(tokenJWT);

                // 3. MUDANÇA: Busca o usuário pela COMBINAÇÃO CORRETA
                usuarioRepository.findByEmailAndDominio(subject, dominio).ifPresent(usuario -> {
                    // Se encontrar, autentica o usuário para esta requisição
                    var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });

            } catch (Exception e) {
                // Se o token for inválido (expirado, assinatura errada), limpa o contexto
                SecurityContextHolder.clearContext();
            }
        }

        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }

    // (Este método já estava 100% correto)
    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }
}