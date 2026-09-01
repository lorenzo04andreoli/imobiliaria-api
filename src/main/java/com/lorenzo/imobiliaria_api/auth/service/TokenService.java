package com.lorenzo.imobiliaria_api.auth.service;

import com.lorenzo.imobiliaria_api.usuario.Usuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
public class TokenService {

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-minutes:120}")
    private Long expirationMinutes;

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiracao = calcularExpiracao(agora);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("role", usuario.getRole().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(getSigningKey())
                .compact();
    }

    public Long getExpirationSeconds() {
        return expirationMinutes * 60;
    }

    public Optional<String> buscarSubject(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            return Optional.ofNullable(subject);
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Instant calcularExpiracao(Instant agora) {
        return agora.plus(expirationMinutes, ChronoUnit.MINUTES);
    }

    private SecretKey getSigningKey() {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("JWT secret nao configurado");
        }

        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
