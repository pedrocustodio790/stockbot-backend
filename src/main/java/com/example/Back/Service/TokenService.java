package com.example.Back.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.Back.Entity.Usuario;
import org.springframework.beans.factory.annotation.Value; // 1. IMPORTAR
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokenService {


    @Value("${api.security.token.secret}")
    private String jwtSecret;

    public String gerarToken(Usuario usuario) {
        List<String> roles = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

        return JWT.create()
                .withIssuer("StockBot API")
                .withSubject(usuario.getEmail())
                .withClaim("id", usuario.getId())
                .withClaim("roles", roles)
                .withExpiresAt(LocalDateTime.now()
                        .plusHours(2)
                        .toInstant(ZoneOffset.of("-03:00"))
                )
                .sign(algorithm);
    }

    public String getSubject(String token) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        return JWT.require(algorithm)
                .withIssuer("StockBot API")
                .build()
                .verify(token)
                .getSubject();
    }
}