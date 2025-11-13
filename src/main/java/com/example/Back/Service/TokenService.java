package com.example.Back.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.Back.Entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    // MUDANÇA: O MÉTODO 'createToken' QUE ESTAVA FALTANDO
    public String createToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("StockBot")
                    .withSubject(usuario.getEmail()) // O "Subject" (dono) é o email
                    .withClaim("dominio", usuario.getDominio()) // ADICIONA A CLAIM DO DOMÍNIO
                    .withExpiresAt(dataExpiracao())
                    .sign(algorithm);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token JWT", e);
        }
    }

    // Este método (que você já tinha) pega o EMAIL (Subject)
    public String getSubject(String tokenJWT) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("StockBot")
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido ou expirado!", exception);
        }
    }

    // MUDANÇA: NOVO MÉTODO para pegar o DOMÍNIO (Claim)
    public String getDominio(String tokenJWT) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("StockBot")
                    .build()
                    .verify(tokenJWT)
                    .getClaim("dominio") // Pega a claim "dominio" que criamos
                    .asString();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido ou expirado (não foi possível pegar o domínio)!", exception);
        }
    }

    private Instant dataExpiracao() {
        // Expira em 2 horas (pode ajustar)
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}