package com.br.pokefichas.commons.security.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.commons.security.model.QRefreshToken;
import com.br.pokefichas.commons.security.model.RefreshToken;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCommand {

    private final JpaRepository repository;

    public RefreshTokenCommand(final JpaRepository repository) {
        this.repository = repository;
    }

    public RefreshToken save(final RefreshToken token) {
        return repository.save(token);
    }

    public void delete(final RefreshToken token) {
        repository.remove(token);
    }

    public void revokeByUsuario(final Usuario usuario) {
        repository.update(RefreshToken.class, (u, p) ->
                u.set(QRefreshToken.refreshToken.isRevoked, true)
                        .where(QRefreshToken.refreshToken.usuario.eq(usuario))
        );
    }
}
