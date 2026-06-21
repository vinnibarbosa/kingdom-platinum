package com.br.pokefichas.commons.security.usecase;

import com.br.pokefichas.commons.exception.AuthenticationException;
import com.br.pokefichas.commons.security.service.RefreshTokenService;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioQuery;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUseCase {

    private final UsuarioQuery usuarioQuery;
    private final RefreshTokenService refreshTokenService;

    public LogoutUseCase(final UsuarioQuery usuarioQuery, final RefreshTokenService refreshTokenService) {
        this.usuarioQuery = usuarioQuery;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void handle() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw AuthenticationException.invalidToken();
        }

        final Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails userDetails)) {
            throw AuthenticationException.invalidToken();
        }

        final Usuario usuario = usuarioQuery.findByUsername(userDetails.getUsername())
                .orElseThrow(AuthenticationException::invalidCredentials);

        refreshTokenService.revokeAllUserTokens(usuario);
        SecurityContextHolder.clearContext();
    }
}
