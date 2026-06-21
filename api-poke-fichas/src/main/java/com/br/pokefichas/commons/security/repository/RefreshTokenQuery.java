package com.br.pokefichas.commons.security.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.commons.security.model.QRefreshToken;
import com.br.pokefichas.commons.security.model.RefreshToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RefreshTokenQuery {

    private final JpaRepository repository;

    public RefreshTokenQuery(final JpaRepository repository) {
        this.repository = repository;
    }

    public Optional<RefreshToken> findByToken(final String token) {
        return repository.findUniqueOptional(
                RefreshToken.class,
                QRefreshToken.refreshToken.token.eq(token)
                        .and(QRefreshToken.refreshToken.isRevoked.eq(false))
        );
    }
}
