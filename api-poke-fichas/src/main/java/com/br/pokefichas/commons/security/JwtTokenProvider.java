package com.br.pokefichas.commons.security;

import com.br.pokefichas.commons.exception.AuthenticationException;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.model.enums.Perfil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_USUARIO_ID = "idUsuario";
    private static final String CLAIM_ID_ENTIDADE = "idEntidade";
    private static final String CLAIM_ID_ORGANIZACAO = "idOrganizacao";
    private static final String CLAIM_PERFIL = "perfil";

    private final String secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${spring.security.jwt.secret}") final String secretKey,
            @Value("${spring.security.jwt.expiration}") final long accessTokenExpiration,
            @Value("${spring.security.jwt.refresh-expiration:2592000000}") final long refreshTokenExpiration) {
        if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET deve possuir pelo menos 32 bytes");
        }
        if (accessTokenExpiration <= 0 || refreshTokenExpiration <= 0) {
            throw new IllegalArgumentException("As expiracoes dos tokens devem ser positivas");
        }
        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(final Usuario usuario) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USUARIO_ID, usuario.getId().toString());
        claims.put(CLAIM_ID_ENTIDADE, Objects.requireNonNull(usuario.getIdEntidade(),
                "Usuario autenticado precisa possuir idEntidade").toString());
        claims.put(CLAIM_ID_ORGANIZACAO, Objects.requireNonNull(usuario.getIdOrganizacao(),
                "Usuario autenticado precisa possuir idOrganizacao").toString());
        claims.put(CLAIM_PERFIL, usuario.getPerfil().name());

        return createToken(claims, usuario.getUsername(), accessTokenExpiration);
    }

    public boolean validateToken(final String token, final String username) {
        try {
            final String tokenUsername = extractUsername(token);
            return username.equals(tokenUsername) && !isTokenExpired(token);
        } catch (final ExpiredJwtException e) {
            throw AuthenticationException.expiredToken();
        } catch (final JwtException | IllegalArgumentException e) {
            throw AuthenticationException.invalidToken();
        }
    }

    public boolean validateToken(final String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (final ExpiredJwtException e) {
            throw AuthenticationException.expiredToken();
        } catch (final JwtException | IllegalArgumentException e) {
            throw AuthenticationException.invalidToken();
        }
    }

    public String extractUsername(final String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractIdUsuario(final String token) {
        final String id = extractAllClaims(token).get(CLAIM_USUARIO_ID, String.class);
        return id != null ? Long.valueOf(id) : null;
    }

    public Perfil extractPerfil(final String token) {
        final String perfil = extractAllClaims(token).get(CLAIM_PERFIL, String.class);
        return Perfil.valueOf(perfil);
    }

    public Long extractIdEntidade(final String token) {
        final String idEntidade = extractAllClaims(token).get(CLAIM_ID_ENTIDADE, String.class);
        return idEntidade != null ? Long.valueOf(idEntidade) : null;
    }

    public Long extractIdOrganizacao(final String token) {
        final String idOrganizacao = extractAllClaims(token).get(CLAIM_ID_ORGANIZACAO, String.class);
        return idOrganizacao != null ? Long.valueOf(idOrganizacao) : null;
    }

    public Date extractExpiration(final String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(final String token) {
        return extractExpiration(token).before(new Date());
    }

    public LocalDateTime getRefreshTokenExpiryDate() {
        return LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);
    }

    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }

    public long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpiration / 1000;
    }

    private String createToken(final Map<String, Object> claims, final String subject, final long expiration) {
        final Instant now = Instant.now();
        final Instant expiryDate = now.plusMillis(expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(final String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
