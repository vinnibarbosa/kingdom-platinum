package com.br.pokefichas.commons.security.service;

import com.br.pokefichas.commons.exception.AuthenticationException;
import com.br.pokefichas.commons.security.JwtTokenProvider;
import com.br.pokefichas.commons.security.model.RefreshToken;
import com.br.pokefichas.commons.security.repository.RefreshTokenCommand;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class RefreshTokenService {

    private static final char[] TOKEN_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int TOKEN_LENGTH = 64;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenCommand refreshTokenCommand;
    private final JwtTokenProvider jwtTokenProvider;

    public RefreshTokenService(final RefreshTokenCommand refreshTokenCommand,
                               final JwtTokenProvider jwtTokenProvider) {
        this.refreshTokenCommand = refreshTokenCommand;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public RefreshToken createRefreshToken(final Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario nao pode ser nulo");
        }

        refreshTokenCommand.revokeByUsuario(usuario);

        final String token = generateToken();
        final LocalDateTime expiryDate = jwtTokenProvider.getRefreshTokenExpiryDate();

        final RefreshToken refreshToken = RefreshToken.Builder
                .create()
                .token(token)
                .usuario(usuario)
                .expiresAt(expiryDate)
                .build(false);

        return refreshTokenCommand.save(refreshToken);
    }

    @Transactional
    public RefreshToken verifyExpiration(final RefreshToken refreshToken) {
        if (refreshToken == null) {
            throw AuthenticationException.invalidToken();
        }
        if (refreshToken.isExpired()) {
            refreshTokenCommand.delete(refreshToken);
            throw AuthenticationException.expiredToken();
        }
        if (refreshToken.getRevoked() != null && refreshToken.getRevoked()) {
            throw AuthenticationException.invalidToken();
        }
        return refreshToken;
    }

    @Transactional
    public void revokeAllUserTokens(final Usuario usuario) {
        if (usuario != null) {
            refreshTokenCommand.revokeByUsuario(usuario);
        }
    }

    private String generateToken() {
        final StringBuilder tokenBuilder = new StringBuilder(TOKEN_LENGTH);
        for (int index = 0; index < TOKEN_LENGTH; index++) {
            tokenBuilder.append(TOKEN_ALPHABET[SECURE_RANDOM.nextInt(TOKEN_ALPHABET.length)]);
        }
        return tokenBuilder.toString();
    }
}
